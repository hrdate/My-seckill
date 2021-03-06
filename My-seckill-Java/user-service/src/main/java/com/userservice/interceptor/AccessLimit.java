package com.userservice.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 自定义封装拦截器注解，默认拦截1秒内访问超过10次的用户
 */
// 允许注解加在方法上
@Target(ElementType.METHOD)
// 允许运行时获取
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimit {
	/**
	 * 时间范围
	 * @return
	 */
	int second() default 1;

	/**
	 * 最大范围次数
	 * @return
	 */
	int maxCount() default 10;

	/**
	 * 超时时间单位 默认:毫秒
	 */
	TimeUnit timeunit() default TimeUnit.SECONDS;
	/**
	 * 默认需要登录
	 * @return
	 */
	boolean needLogin() default true;
}
