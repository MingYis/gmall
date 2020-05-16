package com.my.gmall.all.controller;

import com.my.gmall.cart.client.CartFeignClient;
import com.my.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author mmy
 * @create 2020-05-09 19:12
 * 购物车管理
 */
@Controller
public class CartController {

    @Autowired
    private CartFeignClient cartFeignClient;

    //加入购物车
    @GetMapping("addCart.html")
    public String addCart(Long skuId, Integer skuNum, Model model){

        //保存当前库存到购物车中   保存到购物车表
        CartInfo cartInfo = cartFeignClient.addToCart(skuId, skuNum);
        model.addAttribute("cartInfo",cartInfo);
        return "cart/addCart";
    }

    //去购物车结算
    @GetMapping("/cart.html")
    public String cart(){

        return "cart/index";
    }
}
