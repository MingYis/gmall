package com.my.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.my.gmall.list.dao.GoodsDao;
import com.my.gmall.list.service.ListService;
import com.my.gmall.model.list.*;
import com.my.gmall.model.product.*;
import com.my.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mmy
 * @create 2020-05-02 10:34
 * 搜索管理
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private RestHighLevelClient restHighLevelClient; //ES原生Api包 查询方面性能非常高
    @Autowired
    private GoodsDao goodsDao; //负责 增 删 改
    @Autowired
    private ProductFeignClient productFeignClient;

    //上架库存到ES索引库
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //1.ID
        goods.setId(skuId);
        //2.标题
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setTitle(skuInfo.getSkuName());
        //3.默认图片
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        //4.价格
        goods.setPrice(skuInfo.getPrice().doubleValue());
        //5.创建时间
        goods.setCreateTime(new Date());
        //6.品牌ID
        goods.setTmId(skuInfo.getTmId());
        //7.品牌Name
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        goods.setTmName(trademark.getTmName());
        //8.一二三级分类ID与名称
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        //9.刚保存的索引库 热度评分 默认0
        //10.平台属性集合
        List<SkuAttrValue> skuAttrValueList = productFeignClient.getSkuAttrValue(skuId);
        //jdk1.8 lamda 表达式
        List<SearchAttr> searchAttrList = skuAttrValueList.stream().map((skuAttrValue) -> {
            SearchAttr searchAttr = new SearchAttr();
            //1.平台属性Id
            searchAttr.setAttrId(skuAttrValue.getBaseAttrInfo().getId());
            //2.平台属性名称
            searchAttr.setAttrName(skuAttrValue.getBaseAttrInfo().getAttrName());
            //3.平台属性值名称
            searchAttr.setAttrValue(skuAttrValue.getBaseAttrValue().getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);
        //保存索引
        goodsDao.save(goods);
    }

    //下架库存从ES索引库
    @Override
    public void lowerGoods(Long skuId) {
        goodsDao.deleteById(skuId);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    //更新当前库存热度评分
    @Override
    public void incrHotScore(Long skuId) {
        String key = "hostScore" + skuId;
        //先查询Redis缓存，更新Redis缓存
        //zset  参数1：热度，  参数2：当前库存ID，    参数3：增加的分数，  返回值：当前库存ID的总分类
        Double score = redisTemplate.opsForZSet().incrementScore(key, skuId, 1);
        //判断当前热度评分是否为10,20，30节点，再更新ES
        if (score%10 == 0) {
            //1.从ES中查询此商品
            Optional<Goods> optional = goodsDao.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(score));//四舍五入
            goodsDao.save(goods);//ES 没有更新操作，保存操作如果有对象则覆盖，如果没有则保存
        }
    }

    //开始搜索
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        //1.构建条件对象
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        SearchResponse searchResponse = null;
        try {
            //2.执行查询
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //3.返回结果
        SearchResponseVo vo = processSearchResponseVo(searchResponse);

        //当前页
        vo.setPageNo(searchParam.getPageNo());
        //每页数
        vo.setPageSize(searchParam.getPageSize());
        //总页数 （总条数 + 每页数 - 1）/ 每页数 = 总页数
        Long total = vo.getTotal();
        Integer pageSize = vo.getPageSize();
        vo.setTotalPages((total + (long)pageSize - 1) / (long)pageSize);
        return vo;
    }

    //处理搜索之后的返回结果
    public SearchResponseVo processSearchResponseVo(SearchResponse searchResponse) {
        SearchResponseVo vo = new SearchResponseVo();

        SearchHits hits = searchResponse.getHits();
        //1.总条数
        long totalHits = hits.totalHits;
        System.out.println("总条数" + totalHits);
        vo.setTotal(totalHits);
        //2.商品结果集   List<Goods>
        SearchHit[] hits1 = hits.getHits();
        if (null != hits1 && hits1.length > 0) {
            List<Goods> goodsList = Arrays.stream(hits1).map(searchHit -> {
                //商品数据//商品数据Json格式字符串
                Goods goods = JSON.parseObject(searchHit.getSourceAsString(), Goods.class);
                //判断是否有高亮
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                if(null != highlightFields && highlightFields.size() > 0) {
                    HighlightField title = highlightFields.get("title");
                    String titleHighlight = title.fragments()[0].toString();
                    goods.setTitle(titleHighlight);
                }
                return goods;
            }).collect(Collectors.toList());
            vo.setGoodsList(goodsList);
        }
        //3.品牌结果集   List<SearchResponseTmVo>
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) searchResponse.getAggregations().asMap().get("tmIdAgg");
        List<SearchResponseTmVo> tmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo tmVo = new SearchResponseTmVo();
            //1.品牌ID
            tmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));
            //2.品牌名称
            //在tmIdAgg分组中 遍历每个分组里面有二级分组
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            tmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
            return tmVo;
        }).collect(Collectors.toList());
        vo.setTrademarkList(tmVoList);
        //4.平台属性集合结果解析
        ParsedNested attrsAgg = (ParsedNested) searchResponse.getAggregations().asMap().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
            //1:平台属性ID
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //2;平台属性名称
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //3:平台属性值集合
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> bucketValueList = attrValueAgg.getBuckets().stream().
                    map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValueList(bucketValueList);
            return attrVo;
        }).collect(Collectors.toList());
        //设置平台属性解析结果
        vo.setAttrsList(attrsVoList);
        return vo;
    }

    //构建条件对象并返回
    public SearchRequest buildSearchRequest(SearchParam searchParam) {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //构建条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //searchSourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有索引库的数据
        //1.关键词
        //matchQuery:匹配查询
        //先分词 再查询 我是中国人 我 是 中国 国人 中国人
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword)
                    .operator(Operator.AND));//匹配（可分词）
        }

        //2.过滤条件
        //品牌    品牌的ID、名称 页面传递过来的ID、名称都是字符串类型的 接收的时候 Long类型，SpringMvc转换的
        //品牌ID：品牌名称
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            String[] split = StringUtils.split(trademark, ":");
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));//精准（不可分词）
        }
        //一二三级分类ID
        Long category1Id = searchParam.getCategory1Id();
        if (null != category1Id) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if (null != category2Id) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if (null != category3Id) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", category3Id));
        }
        //平台属性
        String[] props = searchParam.getProps();
        if (null != props) {
            //有平台属性进行过滤
            for (String prop : props) {
                //平台属性ID：平台属性值名称：平台属性名称
                String[] p = prop.split(":");
                //子组合对象
                BoolQueryBuilder subQueryBuilder = QueryBuilders.boolQuery();
                //平台属性Id
                //nestedQuery:嵌套条件对象nestedQuery 参数1：路径，参数2：精准查询,参数3：计算模式
                subQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",p[0]));
                //平台属性值名称
                subQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",p[1]));
                //外面父组合对象 追加多个子组合对象
                boolQueryBuilder.filter(QueryBuilders.
                        nestedQuery("attrs",subQueryBuilder,ScoreMode.None));
            }
        }
        //设置组合对象
        searchSourceBuilder.query(boolQueryBuilder);
        //3.排序  K:V, K:1 2 3 4, 1:综合 2：价格 3：新品 V：desc|asc
        //综合排序：默认是热度评分
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] o = StringUtils.split(order, ":");
            //排序字段
            String orderSort = "";
            switch (o[0]) {
                case "1":
                    orderSort = "hotScore";
                    break;
                case "2":
                    orderSort = "price";
                    break;
            }
            searchSourceBuilder.sort(orderSort,
                    o[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }else {
            //默认按照  热度评分 由高到低排序
            searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        }
        //4.分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        //开始行
        searchSourceBuilder.from((pageNo-1)*pageSize);  //2-1 * 10
        //每页数
        searchSourceBuilder.size(pageSize);
        //5.高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //高亮的字段 ES字段 == 域
        highlightBuilder.field("title");
        //前缀
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //6.分组查询
        //品牌分组查询
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                            .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName")));
        //平台属性分组查询
        searchSourceBuilder.aggregation(
                AggregationBuilders.nested("attrsAgg","attrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //指定查询的索引库
        searchRequest.indices("goods");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
















