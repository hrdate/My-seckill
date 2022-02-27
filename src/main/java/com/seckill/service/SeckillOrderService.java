package com.seckill.service;

import com.seckill.entity.SeckillOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
public interface SeckillOrderService extends IService<SeckillOrder> {

    /**
      * 获取秒杀结果
      * @param user
      * @param goodsId
      * @return
      */
    Long getResult(User user, Long goodsId);
}
