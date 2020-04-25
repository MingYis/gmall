package com.my.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.my.gmall.item.service.ItemService;
import com.my.gmall.model.product.BaseCategoryView;
import com.my.gmall.model.product.SkuInfo;
import com.my.gmall.model.product.SpuSaleAttr;
import com.my.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-23 22:24
 * 商品详情页面数据汇总
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    //查询商品详情页面所有的数据
    @Override
    public Map getItem(Long skuId) {
        Map result = new HashMap();
        //1.库存数据
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        result.put("skuInfo",skuInfo);
        //2.根据三级分类的id查询一二三级分类的名称
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        result.put("categoryView", categoryView);
        //3.单独查询价格
        BigDecimal price = productFeignClient.getPrice(skuId);
        result.put("price", price);
        //4.根据商品ID查询当前销售属性及销售属性值的集合
        //并且根据当前skuId库存ID查询出对应的销售属性值
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
        result.put("spuSaleAttrList",spuSaleAttrListCheckBySku);
        //5.查询组合对应库存ID
        //{颜色|版本|套装：skuId，颜色|版本|套装：skuId}
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        //由于页面需要的是json格式的字符串，所以需要将map格式转为json格式
        result.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));

        return result;
    }
}
