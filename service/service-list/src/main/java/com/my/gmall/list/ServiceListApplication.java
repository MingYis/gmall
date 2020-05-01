package com.my.gmall.list;

/**
 * @author mmy
 * @create 2020-05-01 23:57
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
        exclude = DataSourceAutoConfiguration.class,
        scanBasePackages = {"com.my.gmall"})//取消数据源自动配置
@EnableFeignClients(basePackages = {"com.my.gmall"})
public class ServiceListApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceListApplication.class);
    }
}

