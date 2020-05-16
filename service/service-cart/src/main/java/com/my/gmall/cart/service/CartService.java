package com.my.gmall.cart.service;

import com.my.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @author mmy
 * @create 2020-05-09 19:27
 */
public interface CartService {
    CartInfo attToCart(Long skuId, Integer skuNum, String userId);

    List<CartInfo> cartList(String userId, String userTempId);

    void checkCart(Long skuId, Integer isChecked);

    List<CartInfo> getCartCheckedList(String userId);

}
