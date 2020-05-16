package com.my.gmall.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.my.gmall.model.user.UserInfo;
import com.my.gmall.user.mapper.UserInfoMapper;
import com.my.gmall.user.service.PassportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author mmy
 * @create 2020-05-08 17:29
 */
@Service
public class PassportServiceImpl implements PassportService{

    @Autowired
    private UserInfoMapper userInfoMapper;

    //判断用户名密码是否正确
    @Override
    public UserInfo login(UserInfo userInfo) {
        //密码被加密
        userInfo.setPasswd(DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes()));
        userInfo = userInfoMapper.selectOne(
                new QueryWrapper<UserInfo>().eq("login_name", userInfo.getLoginName())
                        .eq("passwd", userInfo.getPasswd()));
        return userInfo;
    }
}
