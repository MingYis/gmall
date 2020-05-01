package com.my.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mmy
 * @create 2020-04-30 10:34
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {

        return new ThreadPoolExecutor(
                50, 500,
                30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));
    }
}

