package com.my.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author mmy
 * @create 2020-05-09 19:21
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.my.gmall")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.my.gmall")
public class ServiceCartApplication {
    public static void main(String[] args) {

        SpringApplication.run(ServiceCartApplication.class,args);
    }
}
