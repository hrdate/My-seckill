package com.gateway.feign;


import com.gateway.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "user-service")
public interface UserClient {

    @GetMapping("/user/{id}")
    User findUserById(@PathVariable("id") long id);
}
