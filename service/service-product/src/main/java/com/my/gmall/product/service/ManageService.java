package com.my.gmall.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.gmall.model.product.*;

import java.util.List;

/**
 * @author mmy
 * @create 2020-04-19 0:24
 */
public interface ManageService {
    //获取一级分类
    List<BaseCategory1> getCategory1();

    //获取二级分类
    List<BaseCategory2> getCategory2(Long category1);

    //获取三级分类
    List<BaseCategory3> getCategory3(Long category2);

    //获取分类id获取平台属性
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    //保存平台属性和属性值
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    //获取品牌分页列表
    IPage<BaseTrademark> baseTrademark(Integer page, Integer limit);

    //根据三级分类id查询商品分页集合
    IPage<SpuInfo> getSpuInfo(Integer page, Integer limit, Long category3Id);

    //获取品牌属性
    List<BaseTrademark> getTrademarkList();

    //获取销售属性
    List<BaseSaleAttr> baseSaleAttrList();

    //保存商品信息    四张表
    void saveSpuInfo(SpuInfo spuInfo);

    //根据SpuID查询图片列表
    List<SpuImage> spuImageList(Long spuId);

    //根据spuId获取销售属性
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    //添加sku
    void saveSkuInfo(SkuInfo skuInfo);

    //获取sku分页列表
    IPage<SkuInfo> skuList(Integer page, Integer limit);

    //上架
    void onSale(Long skuId);
}
