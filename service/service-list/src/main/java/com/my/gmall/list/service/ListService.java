package com.my.gmall.list.service;

import com.my.gmall.model.list.SearchParam;
import com.my.gmall.model.list.SearchResponseVo;

/**
 * @author mmy
 * @create 2020-05-02 10:33
 */
public interface ListService {
    //上架库存到ES索引库
    void upperGoods(Long skuId);

    //下架库存从ES索引库
    void lowerGoods(Long skuId);

    //更新当前库存热度评分
    void incrHotScore(Long skuId);

    SearchResponseVo list(SearchParam searchParam);
}
