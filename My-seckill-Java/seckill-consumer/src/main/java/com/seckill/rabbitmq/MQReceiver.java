package com.seckill.rabbitmq;

import com.rabbitmq.client.Channel;
import com.seckill.entity.*;
import com.seckill.feign.GoodsClient;
import com.seckill.service.OrderService;
import com.seckill.service.SeckillOrderService;
import com.seckill.utils.JsonUtil;
import com.seckill.vo.GoodsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

import static com.seckill.config.RabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MQReceiver {

    private final RedisTemplate redisTemplate;
    private final OrderService orderService;
    private final SeckillOrderService seckillOrderService;
    private final GoodsClient goodsClient;
    private final static String MESSAGE_CONSUMER_FLAG = "1";

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = QUEUE_SECKILL), key = {ROUTINGKEY_SECKILL},
                    exchange = @Exchange(type = "topic",name = EXCHANGE_SECKILL)
            )
    })
    public void receive(Channel channel, Message message) throws IOException {
        log.info("Rabbitmq队列receive接受消息:" + message.getMessageProperties().getMessageId());
        //消息投递序号
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(message.getMessageProperties().getMessageId(), MESSAGE_CONSUMER_FLAG);
            if(setIfAbsent){
                String messageBody = message.getBody().toString();
                if (StringUtils.isNotBlank(messageBody)) {
                    SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(messageBody, SeckillMessage.class);
                    Long goodsId = seckillMessage.getGoodsId();
                    User user = seckillMessage.getUser();
                    GoodsVo goods = goodsClient.findGoodsVoByGoodsId(goodsId);
                    //判断库存
                    if (goods.getStockCount() < 1) {
                        return;
                    }
                    //判断是否重复抢购
                    String seckillOrderJson = (String)
                            redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
                    if (!StringUtils.isEmpty(seckillOrderJson)) {
                        return;
                    }
                    orderService.seckill(user, goods);
                }
                //deliveryTag:该消息的index,multiple：是否批量.true:将一次性ack所有小于deliveryTag的消息。
                //第三个参数：requeue：重回队列。如果设置为true，则消息重新回到queue，broker会重新发送该消息给消费端
                channel.basicAck(deliveryTag,false);
            } else {
                channel.basicAck(deliveryTag,false);
                log.warn("消息{}已经被完成，忽略",message.getMessageProperties().getMessageId());
            }
        }catch (Exception exception) {
            log.error("消息{}消费过程异常错误",message.toString());
//            deliveryTag:该消息的index
//            multiple：是否批量.true:将一次性拒绝所有小于deliveryTag的消息。
//            requeue：被拒绝的是否重新入队列
            channel.basicNack(deliveryTag,false,true);
        }

    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = QUEUE_DELAY_SECKILL), key = {ROUTINGKEY_DELAY_SECKILL},
                    exchange = @Exchange(type = "topic",name = EXCHANGE_DELAY_SECKILL)
            )
    })
    public void deadReceive(Channel channel, Message message) throws IOException {
        log.info("Rabbitmq死信队列deadReceive接受消息:" + message.getMessageProperties().getMessageId());
        //消息投递序号
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageBody = message.getBody().toString();
        if (StringUtils.isNotBlank(messageBody)) {
            DelayCancelOrderMessage orderMessage = JsonUtil.jsonStr2Object(messageBody, DelayCancelOrderMessage.class);
            Long orderId = orderMessage.getOrderId();
            Long seckillOrderId = orderMessage.getSeckillOrder();
            Order order = orderService.getById(orderId);
            Integer status = order.getStatus();
            if(status == 0) {
                // todo 逻辑删除订单
                SeckillOrder seckillOrder = seckillOrderService.getById(seckillOrderId);
                if(Objects.nonNull(seckillOrder)) {
                    // todo 且需要查看是否需要删除秒杀订单
                }
            }
        }
        channel.basicAck(deliveryTag,false);
    }
}
