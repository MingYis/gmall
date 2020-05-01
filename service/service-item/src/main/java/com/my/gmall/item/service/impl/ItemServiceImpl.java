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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author mmy
 * @create 2020-04-23 22:24
 * 商品详情页面数据汇总
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    //查询商品详情页面所有的数据
    @Override
    public Map getItem(Long skuId) {
        Map result = new HashMap();
        //1.库存数据
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);
        //2.根据三级分类的id查询一二三级分类的名称
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        }, threadPoolExecutor);
        //3.单独查询价格
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getPrice(skuId);
            result.put("price", price);
        }, threadPoolExecutor);

        //4.根据商品ID查询当前销售属性及销售属性值的集合
        //并且根据当前skuId库存ID查询出对应的销售属性值
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);
        //5.查询组合对应库存ID
        //{颜色|版本|套装：skuId，颜色|版本|套装：skuId}
        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //由于页面需要的是json格式的字符串，所以需要将map格式转为json格式
            result.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        }), threadPoolExecutor);

        //主线程执行完了，必须等待所有线程完成
        CompletableFuture.allOf(skuInfoCompletableFuture,categoryViewCompletableFuture,priceCompletableFuture,
                spuSaleAttrListCompletableFuture, valuesSkuJsonCompletableFuture).join();

        return result;
    }
}
