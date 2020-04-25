package com.my.gmall.all.controller;

import com.my.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-25 15:37
 * 加载商品详情页面的所有数据，并渲染商品详情页面进行响应
 */
@Controller
public class ItemController {

    @Autowired
    private ItemFeignClient itemFeignClient;

    //进入商品详情页面中
    @GetMapping("/{skuId}.html")
    public String toItemDetail(@PathVariable(name = "skuId") Long skuId, Model model){

        //远程调用商品详情页面所需要的所有数据汇总
        Map result = itemFeignClient.getItem(skuId);

        //将此map中的数据保存在request域中
        model.addAllAttributes(result); //遍历map中的 K，V，放到request.setAttributes(k, V)中

        //跳转页面
        return "item/index";
    }

}
