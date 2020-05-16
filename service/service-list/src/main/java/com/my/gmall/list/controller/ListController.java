package com.my.gmall.list.controller;

import com.my.gmall.common.result.Result;
import com.my.gmall.list.service.ListService;
import com.my.gmall.model.list.Goods;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mmy
 * @create 2020-05-02 10:01
 */
@Api(tags = "管理索引库")
@RestController
@RequestMapping("/api/list")
public class ListController {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ListService listService;

    //对象映射ES索引库
    @ApiOperation("对象映射ES索引库")
    @GetMapping("inner/createIndex")
    public Result createIndex(){
        //创建索引库
        elasticsearchRestTemplate.createIndex(Goods.class);
        //映射表关系
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    //上架库存到ES索引库
    @ApiOperation("上架库存到ES索引库")
    @GetMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable Long skuId){
        listService.upperGoods(skuId);
        return Result.ok();
    }

    //下架库存从ES索引库
    @ApiOperation("下架库存从ES索引库")
    @GetMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable Long skuId){
        listService.lowerGoods(skuId);
        return Result.ok();
    }

    //更新当前库存热度评分
    @ApiOperation("更新当前库存热度评分")
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable(name = "skuId") Long skuId){
        listService.incrHotScore(skuId);
        return Result.ok();
    }

    //
}
