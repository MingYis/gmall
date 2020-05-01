package com.my.gmall.product.client.impl;

import com.my.gmall.model.product.BaseCategoryView;
import com.my.gmall.model.product.SkuInfo;
import com.my.gmall.model.product.SpuSaleAttr;
import com.my.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-23 22:20
 * 对外暴露的接口 降级实现类
 */
@Component
public class ProductDegradeFeignClient implements ProductFeignClient {

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        return null;
    }

    @Override
    public List<Map> getBaseCategoryList() {
        return null;
    }
}
