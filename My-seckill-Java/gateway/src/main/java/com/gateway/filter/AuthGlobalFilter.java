package com.gateway.filter;

import com.gateway.entity.User;
import com.gateway.feign.UserClient;
import com.gateway.utils.JwtUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;


/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private static Logger LOGGER = LoggerFactory.getLogger(AuthGlobalFilter.class);

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserClient userClient;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //跳过不需要验证的路径
        String url = exchange.getRequest().getURI().getPath();
        if(!StringUtils.isEmpty(url) && url.contains("/login/") ) {
            return chain.filter(exchange);
        }
        try {
            // 获取token
            String token = exchange.getRequest().getHeaders().getFirst("Authorization");
            ServerHttpResponse response = exchange.getResponse();
            // 判断token不为空
            if (StringUtils.isEmpty(token)) {
                authError(response,"授权失败,token不能为空");
                // 拦截请求
                return exchange.getResponse().setComplete();
            }
            // 判断token是否过期
            Claims claims = jwtUtil.getClaimByToken(token);
            if (jwtUtil.isTokenExpired(claims.getExpiration())) {
                authError(response,"授权失败,token已过期");
                // 拦截请求
                return exchange.getResponse().setComplete();
            }
            // 获取token携带信息并反序列化
            String userJson = claims.getSubject();
            User user = new Gson().fromJson(userJson, User.class);
            if(null == user.getId()) {
                authError(response,"授权失败,token已失效");
                // 拦截请求
                return exchange.getResponse().setComplete();
            }
            // 通过fegin查询user-service模块确认token中的用户合法性
            User clientUserById = userClient.findUserById(user.getId());
            if(null == clientUserById) {
                authError(response,"授权失败,token已失效");
                // 拦截请求
                return exchange.getResponse().setComplete();
            }
            // 从token中解析用户信息并设置到Header中去
            LOGGER.info("AuthGlobalFilter.filter() user:{}", userJson);
            ServerHttpRequest request = exchange.getRequest().mutate().header("user", userJson).build();
            exchange = exchange.mutate().request(request).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chain.filter(exchange);
    }

    private Mono<Void> authError(ServerHttpResponse response,String message) {
        response.getHeaders().add("Content-Type", "text/json;charset=UTF-8");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", HttpStatus.UNAUTHORIZED);
        jsonObject.put("message", message);
        byte[] bits = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bits);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // order值越小，优先级越高，执行顺序越靠前
        return -1;
    }
}
