package com.payservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.payservice.entity.Order;
import com.payservice.mapper.OrderMapper;
import com.payservice.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import comment.OrderStatusEnum;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-05-07
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public boolean savaCodeURLById(Long orderId, String codeUrl) {
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",orderId).set("code_url", codeUrl);
        return this.update(updateWrapper);
    }


    @Override
    public Integer getOrderStatus(Long orderId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("id",orderId);
        Order order = this.getOne(wrapper);
        return order.getStatus();
    }

    @Override
    public Boolean updateStatusByOrderNo(Long orderId, Integer status) {
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",orderId).set("status", status);
        return this.update(updateWrapper);
    }
}
