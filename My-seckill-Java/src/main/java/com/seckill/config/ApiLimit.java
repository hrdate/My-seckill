package com.seckill.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

// 允许注解加在方法上
@Target(ElementType.METHOD)
// 允许运行时获取
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiLimit {


    /**
     * 每秒生成令牌数，每秒查询速率
     */
    double QPS() default 50D;

    /**
     * 未获取到令牌允许等待时长（单位毫秒）
     * 获取令牌等待超时时间
     */
    long waitTime() default 500;

    /**
     * 超时时间单位 默认:毫秒
     */
    TimeUnit timeunit() default TimeUnit.MILLISECONDS;

    /**
     * 默认需要登录
     */
    boolean needLogin() default true;
}

