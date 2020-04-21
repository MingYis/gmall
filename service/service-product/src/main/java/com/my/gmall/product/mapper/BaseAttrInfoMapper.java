package com.my.gmall.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.my.gmall.model.product.BaseAttrInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author mmy
 * @create 2020-04-19 15:47
 */
@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {


    List<BaseAttrInfo> attrInfoList(
            @Param("category1Id") Long category1Id,
            @Param("category2Id") Long category2Id,
            @Param("category3Id") Long category3Id);
}
