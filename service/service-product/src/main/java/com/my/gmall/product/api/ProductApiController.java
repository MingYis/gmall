package com.my.gmall.product.api;

import com.my.gmall.common.result.Result;
import com.my.gmall.model.product.BaseCategoryView;
import com.my.gmall.model.product.SkuInfo;
import com.my.gmall.model.product.SpuSaleAttr;
import com.my.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.Name;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-23 16:51
 * 对外暴露的接口
 */
@Api(tags = "对外暴露的前台接口")
@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    //建议使用原来的service
    @Autowired
    private ManageService manageService;

    //根据skuId查询库存表
    @ApiOperation("根据skuId查询库存表")
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(name = "skuId") Long skuId){
        return manageService.getSkuInfo(skuId);
    }

    //根据三级分类的id查询一二三级分类的名称
    @ApiOperation("根据三级分类的id查询一二三级分类的名称")
    @GetMapping("/getCategoryView{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable(name = "category3Id") Long category3Id){
        return manageService.getCategoryView(category3Id);
    }

    //单独查询价格
    @ApiOperation("单独查询价格")
    @GetMapping("/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable(name = "skuId") Long skuId){
        return manageService.getPrice(skuId);
    }

    //-- 根据商品ID查询当前销售属性及销售属性值的集合
    //-- 并且根据当前skuId库存ID查询出对应的销售属性值
    @ApiOperation("根据商品ID查询当前销售属性及销售属性值的集合")
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable(name = "skuId") Long skuId,
                                                          @PathVariable(name = "spuId") Long spuId){
        return manageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    //查询组合对应库存ID
    //{颜色|版本|套装：skuId，颜色|版本|套装：skuId}
    @ApiOperation("查询组合对应库存ID")
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable(name = "spuId") Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }

}
