package com.userservice.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.userservice.config.UserContext;
import comment.RespBean;
import comment.RespBeanEnum;
import com.userservice.entity.User;
import com.userservice.service.UserService;
import com.userservice.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义封装拦截器，拦截1秒内访问超过5次的用户
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

	@Autowired
	private RedisTemplate redisTemplate;


	/**
	 * 拦截执行之前
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {
			User user = getUser(request, response);
			//把user信息放入ThreadLocal中
			UserContext.setUser(user);
			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			if (accessLimit == null) {
				return true;
			}
			int second = accessLimit.second();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if (needLogin) {
				if (user == null) {
					render(response, RespBeanEnum.SESSION_ERROR);
					return false;
				}
				key += ":" + user.getId();
			}
			ValueOperations valueOperations = redisTemplate.opsForValue();
			Integer count = (Integer) valueOperations.get(key);
			if (count == null) {
				valueOperations.set(key, 1, second, accessLimit.timeunit());
			} else if (count < maxCount) {
				valueOperations.increment(key);
			} else {
				render(response, RespBeanEnum.ACCESS_LIMIT_REAHCED);
				return false;
			}
		}
		return true;
	}


	/**
	 * 功能描述: 构建返回对象
	 *
	 */
	private void render(HttpServletResponse response, RespBeanEnum respBeanEnum) throws IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		RespBean respBean = RespBean.error(respBeanEnum);
		out.write(new ObjectMapper().writeValueAsString(respBean));
		out.flush();
		out.close();
	}

	/**
	 * 功能描述: 获取当前登录用户
	 */
	private User getUser(HttpServletRequest request, HttpServletResponse response) {
		String userJson = request.getHeader("user");
		if(userJson == null) {
			return null;
		}
		User user = new Gson().fromJson(userJson, User.class);
		return user;
	}
}
