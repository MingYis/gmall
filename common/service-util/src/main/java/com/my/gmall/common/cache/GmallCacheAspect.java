package com.my.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.my.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point){

        /*
        1.  获取参数列表
        2.  获取方法上的注解
        3.  获取前缀
        4.  获取目标方法的返回值
         */
        Object result = null;
        try {
            Object[] args = point.getArgs();

            MethodSignature signature = (MethodSignature) point.getSignature();
            GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
            // 前缀
            String prefix = gmallCache.prefix();
            // 从缓存中获取数据
            String key = prefix+Arrays.asList(args).toString();

            // 获取缓存数据
            result = cacheHit(signature, key);
            if (null != result){
                // 缓存有数据
                return result;
            }
            // 初始化分布式锁
            RLock lock = redissonClient.getLock(key);
            boolean flag = lock.tryLock(10, 10, TimeUnit.SECONDS);
            if (flag){
                try {
                    result = point.proceed(point.getArgs());
                    // 防止缓存穿透
                    if (null == result){
                        // 并把结果放入缓存
                        Object o = new Object();
                        this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(o));
                        return null;
                    }

                    // 并把结果放入缓存
                    this.redisTemplate.opsForValue().set(key, JSONObject.toJSONString(result));
                    return result;
                }catch (Exception e){
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询缓存的方法
     *
     * @param signature
     * @param key
     * @return
     */
    private Object cacheHit(MethodSignature signature, String key) {
        // 1. 查询缓存
        String cache = (String)redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(cache)) {
            // 有，则反序列化，直接返回
            Class returnType = signature.getReturnType(); // 获取方法返回类型
            // 不能使用parseArray<cache, T>，因为不知道List<T>中的泛型
            return JSONObject.parseObject(cache, returnType);
        }
        return null;
    }
}
