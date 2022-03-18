package com.seckill.rabbitmq;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rabbitmq.client.Channel;
import com.seckill.entity.SeckillMessage;
import com.seckill.entity.SeckillOrder;
import com.seckill.entity.User;
import com.seckill.service.GoodsService;
import com.seckill.service.OrderService;
import com.seckill.service.SeckillOrderService;
import com.seckill.utils.JsonUtil;
import com.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import static com.seckill.config.RabbitMQConfig.*;

@Service
@Slf4j
public class MQReceiver {

    private static Logger logger =   LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue(name = QUEUE_SECKILL), key = {ROUTINGKEY_SECKILL},
                    exchange = @Exchange(type = "topic",name = EXCHANGE_SECKILL)
            )
    })
    public void receive(Channel channel, Message message) throws IOException {
        logger.info("QUEUE接受消息:" + message.getMessageProperties().getMessageId());

        //消息投递序号
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            if(redisTemplate.opsForValue().setIfAbsent(message.getMessageProperties().getMessageId(),"1")){
                SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message.getBody().toString(), SeckillMessage.class);
                Long goodsId = seckillMessage.getGoodsId();
                User user = seckillMessage.getUser();
                GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
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
                //deliveryTag:该消息的index,multiple：是否批量.true:将一次性ack所有小于deliveryTag的消息。
                //第三个参数：requeue：重回队列。如果设置为true，则消息重新回到queue，broker会重新发送该消息给消费端
                channel.basicAck(deliveryTag,false);
            } else {
                channel.basicAck(deliveryTag,false);
                logger.warn("消息{}已经被完成，忽略",message.getMessageProperties().getMessageId());
            }
        }catch (Exception exception) {
            logger.error("消息{}消费过程异常错误",message.toString());
//            deliveryTag:该消息的index
//            multiple：是否批量.true:将一次性拒绝所有小于deliveryTag的消息。
//            requeue：被拒绝的是否重新入队列
            channel.basicNack(deliveryTag,false,true);
        }

    }
}
