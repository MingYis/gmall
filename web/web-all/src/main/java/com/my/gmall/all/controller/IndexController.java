package com.my.gmall.all.controller;


import com.my.gmall.common.result.Result;
import com.my.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author mmy
 * @create 2020-04-30 15:43
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    //初始化首页 在host中配置www.gmall.com
    /*@GetMapping("/")
    public String index(Model model){
        List<Map> listMap = productFeignClient.getBaseCategoryList();
        model.addAttribute("list",listMap);
        return "index/index";
    }*/

    //D:\develop\IdeaProjects\gmall-parent\web\web-all\target\classes\templates
    //静态化技术
    @Autowired
    private TemplateEngine templateEngine;

    //获取首页
    @GetMapping("/")
    public String index(){
        return "index";
    }
    //使用静态化技术生成一个页面
    @GetMapping("/createHtml")
    @ResponseBody
    public Result createHtml(){
        //1.生成页面    数据 + 模板 == 输出
        //数据
        List<Map> listMap = productFeignClient.getBaseCategoryList();
        Context context = new Context();
        //设置数据  context 相当于model
        context.setVariable("list", listMap);
        //输出
        Writer out = null;
        try {
            //编码：写：utf-8
            out = new PrintWriter(new File("D:\\develop\\IdeaProjects\\gmall-parent\\web\\web-all\\target\\classes\\templates\\index.html"),"utf-8");
            //模板    读：utf-8
            templateEngine.process("index/index", context, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }




        return Result.ok();
    }

}
