package com.my.gmall.all.controller;

import com.my.gmall.list.client.ListFeignClient;
import com.my.gmall.model.list.SearchParam;
import com.my.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mmy
 * @create 2020-05-07 21:52
 * 搜索管理之渲染搜索页面
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;
    //进入搜索页面
    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model){
        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);
        //1.入参要进行回显
        model.addAttribute("searchParam",searchParam);
        //2.回显品牌集合
        model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
        //3.回显平台属性
        model.addAttribute("attrsList",searchResponseVo.getAttrsList());
        //4.回显排序 （样式底色 orderMap.type 1.综合排序的 2.价格排序
        Map orderMap = makeOrderMap(searchParam);
        model.addAttribute("orderMap",orderMap);
        //5.回显商品集合
        model.addAttribute("goodsList",searchResponseVo.getGoodsList());
        //6.当前页、每页数
        model.addAttribute("pageNo",searchResponseVo.getPageNo());
        model.addAttribute("totalPages",searchResponseVo.getTotalPages());
        //7.urlParam 当前请求路径？当前入参
        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        return "list/index";//路径名
    }

    //构建URL + 入参
    private String makeUrlParam(SearchParam searchParam) {
        //字符串拼接
        StringBuilder param = new StringBuilder();
        //关键词
        String keyword = searchParam.getKeyword();
        if(!StringUtils.isEmpty(keyword)) {
            param.append("keyword=").append(keyword);
        }
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)) {
            if (param.length() > 0){
                param.append("&trademark=").append(trademark);
            }else {
                param.append("trademark=").append(trademark);
            }
        }

        Long category1Id = searchParam.getCategory1Id();
        if(null != category1Id) {
            if (param.length() > 0){
                param.append("&category1Id=").append(category1Id);
            }else {
                param.append("category1Id=").append(category1Id);
            }
        }
        Long category2Id = searchParam.getCategory2Id();
        if(null != category2Id) {
            if (param.length() > 0){
                param.append("&category2Id=").append(category2Id);
            }else {
                param.append("category2Id=").append(category2Id);
            }
        }
        Long category3Id = searchParam.getCategory3Id();
        if(null != category3Id) {
            if (param.length() > 0){
                param.append("&category3Id=").append(category3Id);
            }else {
                param.append("category3Id=").append(category3Id);
            }
        }
        //平台属性
        String[] props = searchParam.getProps();
        if(null != props && props.length > 0) {
            for (String prop : props) {
                if (param.length() > 0){
                    param.append("&props=").append(prop);
                }else {
                    param.append("props=").append(prop);
                }
            }
        }
        return "list.html?" + param.toString();
    }

    //构建排序回显内容
    private Map makeOrderMap(SearchParam searchParam) {
        Map orderMap = new HashMap();
        //1：判断排序  // order=1:asc  排序规则   0:asc
        String order = searchParam.getOrder();
        if(!StringUtils.isEmpty(order)){
            String[] o = order.split(":");
            orderMap.put("type",o[0]);
            orderMap.put("sort",o[1]);
        }else{
            //如果没有排序  默认使用综合 排序
            orderMap.put("type","1");
            orderMap.put("sort","desc");
        }
        return orderMap;
    }
}
