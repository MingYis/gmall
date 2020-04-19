package com.my.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.my.gmall.model.product.BaseCategory1;
import com.my.gmall.model.product.BaseCategory2;
import com.my.gmall.model.product.BaseCategory3;
import com.my.gmall.product.mapper.BaseCategory1Mapper;
import com.my.gmall.product.mapper.BaseCategory2Mapper;
import com.my.gmall.product.mapper.BaseCategory3Mapper;
import com.my.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
