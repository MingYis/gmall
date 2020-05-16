package com.my.gmall.list.api;

import com.my.gmall.list.service.ListService;
import com.my.gmall.model.list.SearchParam;
import com.my.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mmy
 * @create 2020-05-03 22:33
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {

    @Autowired
    private ListService listService;

    //开始搜索
    @PostMapping
    public SearchResponseVo list(@RequestBody SearchParam searchParam){
        return listService.list(searchParam);
    }
}
