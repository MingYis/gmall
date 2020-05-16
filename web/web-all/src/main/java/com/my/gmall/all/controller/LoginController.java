package com.my.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author mmy
 * @create 2020-05-08 16:12
 */
@Controller
public class LoginController {

    //去登录页面
    @GetMapping("login.html")
    public String login(String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);//放在request域中
        return "login";
    }
}
