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
}
