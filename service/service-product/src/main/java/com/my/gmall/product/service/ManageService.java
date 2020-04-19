package com.my.gmall.product.service;

import com.my.gmall.model.product.BaseCategory1;
import com.my.gmall.model.product.BaseCategory2;
import com.my.gmall.model.product.BaseCategory3;

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
}
