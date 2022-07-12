package com.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.Order;
import com.seckill.entity.User;
import com.seckill.vo.GoodsVo;
import com.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
public interface OrderService extends IService<Order> {


    Order seckill(User user, GoodsVo goods);
}
