package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.entity.Order;

import com.seckill.entity.SeckillGoods;
import com.seckill.entity.SeckillOrder;
import com.seckill.entity.User;
import com.seckill.feign.GoodsClient;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.OrderService;

import com.seckill.service.SeckillGoodsService;
import com.seckill.service.SeckillOrderService;
import com.seckill.utils.RedisUtil;
import com.seckill.vo.GoodsVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final SeckillGoodsService seckillGoodsService;
    private final OrderMapper orderMapper;
    private final SeckillOrderService seckillOrderService;
    private final RedisUtil redisUtil;


    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goods) {
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new LambdaQueryWrapper<SeckillGoods>()
                .eq(SeckillGoods::getGoodsId, goods.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        //执行sql语句，数据库引擎innodb可以保证原子性
        boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = " + "stock_count - 1")
                .eq("goods_id",goods.getId())
                .gt("stock_count",0));
        if (!seckillGoodsResult){
            return null;
        }
        //生成订单
        Order order = new Order();

        order.setUserId(user.getId());

        order.setGoodsId(goods.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goods.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrderService.save(seckillOrder);
//        redisTemplate.opsForValue().set("order:" + user.getId() + ":" +
//                        goods.getId(),
//                    JsonUtil.object2JsonStr(seckillOrder));
        redisUtil.set("order:" + user.getId() + ":" + goods.getId(),seckillOrder);

        return order;
    }
}
