package com.my.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.gmall.model.product.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-22 22:43
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {
    //查询组合对应库存ID
    List<Map> getSkuValueIdsMap(Long spuId);
}
