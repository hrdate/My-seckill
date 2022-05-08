package com.payservice.service;

import com.payservice.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

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
     * 完成支付后，修改订单的状态为已支付
     * @param orderId
     * @return
     */
    boolean updateOrderToPayById(Long orderId);


}
