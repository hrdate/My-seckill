package com.seckill.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.seckill.config.UserContext;
import com.seckill.entity.User;
import comment.RespBean;
import comment.RespBeanEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiLimitInterceptor implements HandlerInterceptor {

    private static Logger logger = LoggerFactory.getLogger(ApiLimitInterceptor.class);
    /**
     * 不同的方法存放不同的令牌桶
     */
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (handler instanceof HandlerMethod) {
            User user = getUser(request, response);
            //把user信息放入ThreadLocal中
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            ApiLimit apiLimit = hm.getMethodAnnotation(ApiLimit.class);
            if(apiLimit == null) {
                return true;
            }
            boolean needLogin = apiLimit.needLogin();
            if (needLogin) {
                if (user == null) {
                    render(response, RespBeanEnum.SESSION_ERROR);
                    return false;
                }
            }
            String url = request.getRequestURI();
            RateLimiter rateLimiter;
            // 判断map集合中是否有创建好的令牌桶
            if (!rateLimiterMap.containsKey(url)) {
            // 创建令牌桶,以n r/s往桶中放入令牌
                rateLimiter = RateLimiter.create(apiLimit.QPS());
                rateLimiterMap.put(url, rateLimiter);
            }
            rateLimiter = rateLimiterMap.get(url);
            // 获取令牌
            boolean acquire = rateLimiter.tryAcquire(apiLimit.waitTime(), apiLimit.timeunit());
            if(!acquire) {
                //获取令牌失败
                render(response, RespBeanEnum.ACCESS_LIMIT_REAHCED);
                logger.warn("用户{}尝试获取令牌失败",user);
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
        User user = new Gson().fromJson(userJson, User.class);
        // 通过fegin查询user-service模块确认token中的用户合法性
        return user;
    }

}
