package com.my.gmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.gmall.model.product.*;
import com.my.gmall.product.mapper.*;
import com.my.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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

}
