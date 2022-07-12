package com.seckill.service;

import com.seckill.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;
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

    /**
      * 秒杀
      * @param user
      * @param goods
      * @return
      */
    Order seckill(User user, GoodsVo goods);
    /**
     * 订单详情
     * @param orderId
     * @return
     */
    OrderDetailVo detail(Long orderId);

    /**
     * 检验秒杀动态路径
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    boolean checkPath(User user, Long goodsId, String path);

    /**
     * 生成秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    String createPath(User user, Long goodsId);

    /**
     * 校验验证码
     * @param user
     * @param goodsId
     * @param captcha
     * @return
     */
    boolean checkCaptcha(User user, Long goodsId, String captcha);
}
