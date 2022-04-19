package com.gateway.resolver;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyKeyResolver implements KeyResolver {
    /**
     * <String> 字符串泛型，代表令牌分给谁,可以是IP地址也可以是数字字符串,如果返回 "1",表示所有客户端一表只有一个令牌
     *
     * @param exchange
     * @return
     */
    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String remoteAddr = request.getRemoteAddress().getAddress().getHostAddress();
        // 对每个客户端IP进行限流
        return Mono.just(remoteAddr);
    }
}