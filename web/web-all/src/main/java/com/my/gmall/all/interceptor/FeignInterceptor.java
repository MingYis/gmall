package com.my.gmall.all.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author mmy
 * @create 2020-05-10 17:41
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

    //拦截器方法 远程调用之前执行的方法
    //参数1： RequestTemplate：将要发出去的远程调用请求
    @Override
    public void apply(RequestTemplate requestTemplate) {

        //1.获取当前页面微服务中的请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(null != requestAttributes){
            HttpServletRequest request = requestAttributes.getRequest();
            HttpServletResponse response = requestAttributes.getResponse();
            //2.从当前请求头中获取用户ID或临时用户的UUID
            String userId = request.getHeader("userId");
            String userTempId = request.getHeader("userTempId");
            //3.将用户ID或临时用户UUID设置到RedisTemplate的请求头中
            if (!StringUtils.isEmpty(userId)){
                requestTemplate.header("userId",userId);
            }
            if (!StringUtils.isEmpty(userTempId)){
                requestTemplate.header("userTempId",userTempId);
            }
        }

    }
}
