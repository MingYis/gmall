package com.my.gmall.item.controller;

import com.my.gmall.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-23 22:25
 */
@RestController
@RequestMapping("/api/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    //数据汇总
    @GetMapping("/{skuId}")
    public Map getItem(@PathVariable Long skuId){
        return itemService.getItem(skuId);
    }

}
