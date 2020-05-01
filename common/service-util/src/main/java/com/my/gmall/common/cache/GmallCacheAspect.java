package com.my.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import com.my.gmall.common.constant.RedisConst;
import lombok.extern.slf4j.Slf4j;
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
@Slf4j
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;//此Redis 保存Value值的时候 此Value要求必须实现序列化接口
    @Autowired
    private RedissonClient redissonClient;//上锁

    //方法  完成缓存的方法
    @Around(value = "@annotation(com.my.gmall.common.cache.GmallCache)")//进入此切面方法的条件
    public Object cacheAspectMethod(ProceedingJoinPoint pjp){
        //获取前缀
        MethodSignature signature = (MethodSignature) pjp.getSignature();//签名==当前方法的  public  返回值  包名+ 类名 + 方法名 + 入参
        Method method = signature.getMethod();


        //1：缓存注解
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //2：返回值 类型 Object无法保存Redis中
        Class returnType = signature.getReturnType();//BaseCategoryView
        //3：入参
        Object[] args = pjp.getArgs();


        //唯一的    @GmallCache(prefix="getBaseCategoryView")
        String prefix = gmallCache.prefix();

        //缓存Key值
        String key = prefix + Arrays.asList(args).toString();//[1,4]
        //1:从缓存中查询   K:String  V：任何类型（底层肯定会将任何类型转成Json格式字符串）
        String o = (String) redisTemplate.opsForValue().get(key);
        if(null != o){
            //2:有  直接返回
            log.info("缓存中有要查询的数据");
            return JSONObject.parseObject(o,returnType);
        }
        //3:没有 查询数据库  保存缓存一份  击穿

        RLock lock = redissonClient.getLock(key + RedisConst.SKULOCK_SUFFIX);
        try {
            boolean tryLock = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1,
                    RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            if(tryLock){
                //切面回到方法中去查询
                Object proceed = pjp.proceed(args);
                //穿透
                if(null == proceed){
                    Object o1 = new Object();
                    String json = JSONObject.toJSONString(o1);
                    //    "{}"
                    redisTemplate.opsForValue().set(key,json,5,TimeUnit.MINUTES);
                    return JSONObject.parseObject("{}",returnType);
                }else{
                    String json = JSONObject.toJSONString(proceed);
                    redisTemplate.opsForValue().set(key,json,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    return proceed;
                }
            }else{
                Thread.sleep(3000);
                return redisTemplate.opsForValue().get(key);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }finally {
            if(lock.isLocked()){
                lock.unlock();
            }
        }
        return JSONObject.parseObject("{}",returnType);//空结果 //BaseCategoryView
    }
}
