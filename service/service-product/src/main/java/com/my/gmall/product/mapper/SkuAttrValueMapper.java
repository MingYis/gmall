package com.my.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.gmall.model.product.SkuAttrValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author mmy
 * @create 2020-04-22 22:42
 */
@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {

    //根据skuId查询平台属性ID、属性名称及属性值
    List<SkuAttrValue> getSkuAttrValue(Long skuId);
}
