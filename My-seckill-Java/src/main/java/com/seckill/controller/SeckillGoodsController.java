package com.seckill.controller;


import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@RestController
@RequestMapping("/seckillGoods")
public class SeckillGoodsController implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
