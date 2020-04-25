package com.my.gmall.item.service;

import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-23 22:23
 */
public interface ItemService {
    //查询商品详情页面所有的数据
    Map getItem(Long skuId);

}
