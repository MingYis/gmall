package com.my.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.gmall.model.product.SpuSaleAttr;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author mmy
 * @create 2020-04-21 17:15
 */
@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    //根据spuId获取销售属性
    List<SpuSaleAttr> spuSaleAttrList(Long spuId);
}
