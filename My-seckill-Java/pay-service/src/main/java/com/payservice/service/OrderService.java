package com.payservice.service;

import com.payservice.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import comment.OrderStatusEnum;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-05-07
 */
public interface OrderService extends IService<Order> {

    /**
     * 保存订单的二维码
     * @param orderId
     * @return
     */
    boolean savaCodeURLById(Long orderId, String codeUrl);


    /**
     * 获取订单的状态
     * @param orderId
     * @return
     */
    Integer getOrderStatus(Long orderId);

    /**
     *
     * @param orderId
     * @param status
     */
    Boolean updateStatusByOrderNo(Long orderId, Integer status);
}
