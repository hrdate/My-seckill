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


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order seckill(User user, GoodsVo goods) {
        //获取秒杀商品的详情
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new LambdaQueryWrapper<SeckillGoods>()
                .eq(SeckillGoods::getGoodsId, goods.getId()));

        //秒杀商品表减库存
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        //执行sql语句，数据库引擎innodb可以保证原子性
        boolean seckillGoodsResult = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = " + "stock_count - 1")
                .eq("goods_id",goods.getId())
                .gt("stock_count",0));
        //扣减库存失败
        if (!seckillGoodsResult){
            return null;
        }

        //扣减库存成功，生成订单
        Order order = new Order();
        // 用户id
        order.setUserId(user.getId());
        // 订单id
        order.setGoodsId(goods.getId());
        // 收获地址ID
        order.setDeliveryAddrId(0L);
        // 商品名
        order.setGoodsName(goods.getGoodsName());
        // 商品数量
        order.setGoodsCount(1);
        // 商品价格
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        // 用户操作系统
        order.setOrderChannel(1);
        // 订单状态，0新建未支付
        order.setStatus(0);
        // 订单创建时间
        order.setCreateDate(new Date());

        // 订单order表添加一行新数据
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(goods.getId());
        // 秒杀订单表中添加一行新数据
        seckillOrderService.save(seckillOrder);
//        redisTemplate.opsForValue().set("order:" + user.getId() + ":" +
//                        goods.getId(),
//                    JsonUtil.object2JsonStr(seckillOrder));

        // 在redis缓存中记录改商品-订单已经秒杀成功
        redisUtil.set("order:" + user.getId() + ":" + goods.getId(),seckillOrder);

        return order;
    }
}
