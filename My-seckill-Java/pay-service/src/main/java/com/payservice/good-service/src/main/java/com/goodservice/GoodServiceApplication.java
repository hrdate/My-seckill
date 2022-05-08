package com.goodservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@MapperScan("com.goodservice.mapper")
@EnableDiscoveryClient
@SpringBootApplication
public class GoodServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoodServiceApplication.class, args);
    }

}
