package com.my.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.gmall.model.product.*;
import com.my.gmall.product.mapper.*;
import com.my.gmall.product.service.ManageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }


}
