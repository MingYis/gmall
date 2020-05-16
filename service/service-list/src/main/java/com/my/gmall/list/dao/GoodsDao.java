package com.my.gmall.list.dao;

import com.my.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author mmy
 * @create 2020-05-02 10:41
 */
public interface GoodsDao extends ElasticsearchRepository<Goods,Long> {
}
