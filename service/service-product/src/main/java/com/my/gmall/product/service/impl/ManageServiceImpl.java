package com.my.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.gmall.common.cache.GmallCache;
import com.my.gmall.common.constant.RedisConst;
import com.my.gmall.model.product.*;
import com.my.gmall.product.mapper.*;
import com.my.gmall.product.service.ManageService;
import io.swagger.annotations.ApiOperation;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author mmy
 * @create 2020-04-19 0:24
 */
@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;

    //获取一级分类
    @Override
    public List<BaseCategory1> getCategory1() {

        //查询所有一级分类
        return baseCategory1Mapper.selectList(null);
    }

    //获取二级分类
    @Override
    public List<BaseCategory2> getCategory2(Long category1) {

        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id",category1);

        return baseCategory2Mapper.selectList(wrapper);

    }

    //获取三级分类
    @Override
    public List<BaseCategory3> getCategory3(Long category2) {
        return baseCategory3Mapper.selectList(
                new QueryWrapper<BaseCategory3>().eq("category2_id",category2));
    }

    //获取分类id获取平台属性
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {

        return baseAttrInfoMapper.attrInfoList(category1Id,category2Id,category3Id);
    }

    //保存平台属性和属性值
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1:保存平台属性
        baseAttrInfoMapper.insert(baseAttrInfo);
        //2:保存平台属性值表 一对多
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(!CollectionUtils.isEmpty(attrValueList)){
            attrValueList.forEach(attrValue ->{
                //平台属性表的id作为外键
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(attrValue);
            });
        }
    }

    //获取品牌分页列表
    @Override
    public IPage<BaseTrademark> baseTrademark(Integer page, Integer limit) {
        IPage<BaseTrademark> pageTrademark = new Page(page,limit);
        IPage<BaseTrademark> page1 = baseTrademarkMapper.selectPage(pageTrademark, null);
        return page1;
    }

    //根据三级分类id查询商品分页集合
    @Override
    public IPage<SpuInfo> getSpuInfo(Integer page, Integer limit, Long category3Id) {
        IPage<SpuInfo> infoIPage = new Page(page, limit);

        return spuInfoMapper.selectPage(
                infoIPage, new QueryWrapper<SpuInfo>().eq("category3_id",category3Id));
    }

    //获取品牌属性
    @Override
    public List<BaseTrademark> getTrademarkList() {

        return baseTrademarkMapper.selectList(null);
    }

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    //获取销售属性
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    //保存商品信息    四张表
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1.保存SpuInfo   商品信息表
        spuInfoMapper.insert(spuInfo);

        //2.保存商品图片表
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        spuImageList.forEach(image ->{
            //商品信息表的ID作为外键
            image.setSpuId(spuInfo.getId());
            spuImageMapper.insert(image);
        });

        //3.保存商品的销售信息
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuSaleAttrList.forEach(saleAttr ->{
            //商品信息表的id作为外键
            saleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(saleAttr);

            //4此商品属性包含多个属性值
            List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
            spuSaleAttrValueList.forEach(saleAttrValue ->{
                //商品表的id作为外键
                saleAttrValue.setSpuId(spuInfo.getId());
                //销售属性的名称
                saleAttrValue.setSaleAttrName(saleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(saleAttrValue);
            });


        });
    }

    //根据SpuID查询图片列表
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id",spuId));
    }

    //根据spuId获取销售属性
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        //mapper只有单表操作，没有关联查询，关联查询需要手写sql
        return spuSaleAttrMapper.spuSaleAttrList(spuId);
    }

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    //添加Sku
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //默认不卖
        skuInfo.setIsSale(0);
        //1.sku_info
        skuInfoMapper.insert(skuInfo);
        //2.sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        skuImageList.forEach(image ->{
            //需要外键
            image.setSkuId(skuInfo.getId());
            skuImageMapper.insert(image);
        });
        //3.销售属性 sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        skuSaleAttrValueList.forEach(saleAttrValue ->{
            saleAttrValue.setSkuId(skuInfo.getId());
            saleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(saleAttrValue);
        });
        //4.保存平台属性 sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        skuAttrValueList.forEach(attrValue ->{
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(attrValue);
        });

    }

    //获取sku分页列表
    @Override
    public IPage<SkuInfo> skuList(Integer page, Integer limit) {
        IPage<SkuInfo> skuInfoIPage = new Page<>(page,limit);

        return skuInfoMapper.selectPage(skuInfoIPage,null);
    }

    //上架
    @Override
    public void onSale(Long skuId) {
        //1.更新库存上架状态
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
        //TODO 增加索引
    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    // 前台页面 根据skuId查询库存表 使用缓存优化 使用Redisson上锁，防止缓存三大问题
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        //1.从缓存中获取SkuInfo信息
        String key = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
        if (null != skuInfo){
            //2.有
            return skuInfo;
        }
        // 缓存击穿问题 百万请求只允许一个请求进入 上锁
        RLock lock = redissonClient.getLock(key + ":lock");
        try {
            boolean tryLock = lock.tryLock(1, 10, TimeUnit.SECONDS);
            if (tryLock){
                //我是代表，是第一人，拿到了锁
                skuInfo = skuInfoMapper.selectById(skuId);
                //缓存三大问题的缓存穿透问题
                if (null == skuInfo){
                    //准备空结果
                    skuInfo = new SkuInfo();
                    redisTemplate.opsForValue().set(key, skuInfo, 5, TimeUnit.MINUTES);
                    return skuInfo;
                }else {
                    //根据skuId查询库存图片表
                    List<SkuImage> skuImageList =
                            skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                    skuInfo.setSkuImageList(skuImageList);
                    //将结果保存在缓存中一份
                    redisTemplate.opsForValue().set(key, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    return skuInfo;
                }
            }else {
                //我不是代表，不是第一人
                Thread.sleep(2000);
                return (SkuInfo) redisTemplate.opsForValue().get(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //解锁
            if (lock.isLocked()){
                lock.unlock();
            }
        }
        //在抛出异常时会执行 去数据库中查
        return getSkuInfoDBById(skuId);
    }

    //根据库存Id查询mysql数据库的skuInfo
    public SkuInfo getSkuInfoDBById(Long skuId){
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //还会遇见穿透问题
        if (null == skuInfo){
            skuInfo = new SkuInfo();
            return skuInfo;
        }
        List<SkuImage> skuImageList =
                skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    /*// 前台页面 根据skuId查询库存表  未使用缓存优化
    @Override
    public SkuInfo getSkuInfo(Long skuId) {

        //1.先从Redis缓存中查找 redis五大数据类型 String Hash List set zset
        //key = 前缀 + skuId + 后缀 封装的常量
        String key = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(key);
        if (null != skuInfo){
            //2.直接返回
            return skuInfo;
        }
        //3.没有再去MySQL数据库查询
        //1).根据skuId查询库存表
        skuInfo = skuInfoMapper.selectById(skuId);
        //防止缓存穿透 判断skuInfo是否为null 如果为null则表示人为攻击，返回空结果
        if (null == skuInfo){
            skuInfo = new SkuInfo();
            redisTemplate.opsForValue().set(key, skuInfo, 5 , TimeUnit.MINUTES);//过期时间为5分钟
            System.out.println("有人攻击网站，skuId不存在，返回空结果");
            return skuInfo;
        }
        //2).根据skuId查询库存图片表
        List<SkuImage> skuImageList =
                skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImageList(skuImageList);

        //防止缓存雪崩 大量缓存同时失效
        Random random = new Random();
        int i = random.nextInt(300);
        //4.再保存缓存一份  key:String类型 V:任何类型，底层转成Json格式字符串
        redisTemplate.opsForValue().set(key, skuInfo, RedisConst.SKUKEY_TIMEOUT + i,TimeUnit.SECONDS); //一天后过期

        //5.返回
        return skuInfo;
    }*/

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    //根据三级分类的id查询一二三级分类的名称
    @Override
    @GmallCache(prefix = "getCategoryView")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    //单独查询价格
    @Override
    public BigDecimal getPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (null != skuInfo) {
            //防止空指针异常
            return skuInfo.getPrice();
        }
        return null;
    }

    //-- 根据商品ID查询当前销售属性及销售属性值的集合
    @Override
    @GmallCache(prefix = "getSpuSaleAttrListCheckBySku")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    //查询组合对应库存ID
    @Override
    @GmallCache(prefix = "getSkuValueIdsMap")
    public Map getSkuValueIdsMap(Long spuId) {
        Map result = new HashMap();
        List<Map> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        skuValueIdsMap.forEach(map ->{
            result.put(map.get("values_id"),map.get("sku_id"));
        });
        return result;
    }

    //查询分类视图对象集合
    @Override
    public List<Map> getBaseCategoryList() {

        //结果对象
        List<Map> result = new ArrayList<>();

        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        //按照一级分类ID进行分组查询
        Map<Long, List<BaseCategoryView>> baseCategoryViewByCategory1Id =
                baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //Map1: k: 1, v: List<BaseCategoryView> ID:1-60 60个
        //Map2: k: 2, v: List<BaseCategoryView> ID:61-85 24个

        //定义角标
        int index = 1;
        //遍历map的entry   快捷键iter
        for (Map.Entry<Long, List<BaseCategoryView>> category1Entry : baseCategoryViewByCategory1Id.entrySet()) {

            Map category1Map = new HashMap();
            //1.角标
            category1Map.put("index", index++);
            //2.一级分类的ID
            category1Map.put("categoryId", category1Entry.getKey());
            //3.一级分类的名称
            category1Map.put("categoryName", category1Entry.getValue().get(0).getCategory1Name());
            //4.二级分类的集合子节点
            //4-1:查询二级分类的集合 按照二级分类ID进行分组查询
            Map<Long, List<BaseCategoryView>> baseCategoryViewByCategory2Id =
                    category1Entry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            //遍历map Map1: k: 1, v: List<BaseCategoryView> ID:1-60 60个
            //创建接收二级分类的集合
            List<Map> result2 = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2Entry : baseCategoryViewByCategory2Id.entrySet()) {
                Map category2Map = new HashMap();

                //4-1-3 三级分类集合
                List<BaseCategoryView> category3Value = category2Entry.getValue();

                //1."categoryName": "电子书刊"
                category2Map.put("categoryName", category3Value.get(0).getCategory2Name());
                //2."categoryId": 1
                category2Map.put("categoryId", category2Entry.getKey());
                //3.三级分类的集合 categoryChild
                List<Map> result3 = new ArrayList<>();
                for (BaseCategoryView baseCategoryView : category3Value) {
                    Map category3Map = new HashMap();
                    //"categoryName": "电子书",
                    //"categoryId": 1
                    category3Map.put("categoryId", baseCategoryView.getCategory3Id());
                    category3Map.put("categoryName", baseCategoryView.getCategory3Name());
                    result3.add(category3Map);
                }

                category2Map.put("categoryChild", result3);
                result2.add(category2Map);
            }

            category1Map.put("categoryChild",result2);

            result.add(category1Map);

        }

        return result;
    }
}
