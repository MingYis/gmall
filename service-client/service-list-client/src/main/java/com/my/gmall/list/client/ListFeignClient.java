package com.my.gmall.list.client;

import com.my.gmall.common.result.Result;
import com.my.gmall.model.list.SearchParam;
import com.my.gmall.model.list.SearchResponseVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author mmy
 * @create 2020-05-03 21:27
 */
@FeignClient("service-list")
public interface ListFeignClient {
    //更新当前库存热度评分
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable(name = "skuId") Long skuId);

    //开始搜索
    @PostMapping("/api/list")
    public SearchResponseVo list(@RequestBody SearchParam searchParam);
}
