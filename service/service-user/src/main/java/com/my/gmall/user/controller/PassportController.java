package com.my.gmall.user.controller;

import com.my.gmall.common.constant.RedisConst;
import com.my.gmall.common.result.Result;
import com.my.gmall.model.user.UserInfo;
import com.my.gmall.user.service.PassportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author mmy
 * @create 2020-05-08 17:19
 * 登录管理
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportController {

    @Autowired
    private PassportService passportService;
    @Autowired
    private RedisTemplate redisTemplate;

    //提交登录
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo){
        if (StringUtils.isEmpty(userInfo.getLoginName())){
            return Result.fail().message("用户名不能为空");
        }

        userInfo = passportService.login(userInfo);
        if (null != userInfo){
            //登录成功
            Map data = new HashMap();
            data.put("nickName",userInfo.getNickName());
            String token = UUID.randomUUID().toString().replaceAll("-", "");//32位令牌
            data.put("token",token);

            //将令牌保存在缓存中 K:令牌 V:userInfo的用户ID
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,
                    userInfo.getId().toString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);


            return Result.ok(data);
        }else {
            //登录失败
            return Result.fail().message("登录失败");
        }

    }

    //退出
    @GetMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
