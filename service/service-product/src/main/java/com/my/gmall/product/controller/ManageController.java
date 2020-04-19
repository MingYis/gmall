package com.my.gmall.product.controller;

import com.my.gmall.common.result.Result;
import com.my.gmall.model.product.BaseCategory1;
import com.my.gmall.model.product.BaseCategory2;
import com.my.gmall.model.product.BaseCategory3;
import com.my.gmall.product.service.ManageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author mmy
 * @create 2020-04-19 0:08
 */
@Api(tags = "后台管理")
@RestController
@RequestMapping("/admin/product") //所有请求的路径都是/admin/product开始的
@CrossOrigin
public class ManageController {

    @Autowired
    private ManageService manageService;

    //获取一级分类
    @ApiOperation("获取一级分类")
    @GetMapping("/getCategory1")
    public Result getCategory1(){

        List<BaseCategory1> baseCategory1List = manageService.getCategory1();

        return Result.ok(baseCategory1List);
    }

    //获取二级分类
    @ApiOperation("获取二级分类")
    @GetMapping("/getCategory2/{category1}")
    public Result getCategory2(@PathVariable Long category1){

        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1);
        return Result.ok(baseCategory2List);
    }

    //获取三级分类
    @ApiOperation("获取三级分类")
    @GetMapping("/getCategory3/{category2}")
    public Result getCategory3(@PathVariable Long category2){

        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2);
        return Result.ok(baseCategory3List);
    }


}
