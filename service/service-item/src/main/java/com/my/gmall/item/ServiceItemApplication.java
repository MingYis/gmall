package com.my.gmall.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author mmy
 * @create 2020-04-23 11:29
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@ComponentScan({"com.my.gmall"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.my.gmall"})
public class ServiceItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceItemApplication.class, args);
    }

}

