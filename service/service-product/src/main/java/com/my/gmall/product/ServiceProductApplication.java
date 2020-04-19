package com.my.gmall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.my.gmall"})
@EnableDiscoveryClient
public class ServiceProductApplication {

   public static void main(String[] args) {
      SpringApplication.run(ServiceProductApplication.class, args);
   }

}

