package com.my.gmall.cart.client;

import com.my.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author mmy
 * @create 2020-05-11 15:31
 */

@FeignClient("service-cart")
public interface CartFeignClient {

    //加入购物车
    @GetMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable(name = "skuId") Long skuId,
                              @PathVariable(name = "skuNum") Integer skuNum);
}
