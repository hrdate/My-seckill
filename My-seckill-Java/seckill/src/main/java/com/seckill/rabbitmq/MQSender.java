package com.seckill.rabbitmq;


import com.seckill.config.RabbitMQConfig;
import com.seckill.entity.SeckillError;
import com.seckill.entity.SeckillMessage;
import com.seckill.service.SeckillErrorService;
import com.seckill.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;


@Service
@Slf4j
public class MQSender {
    private static Logger logger =   LoggerFactory.getLogger(MQSender.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisScript scriptConfirm;
    @Autowired
    SeckillErrorService seckillErrorService;

    public void sendsecKillMessage(String message) {
        //生产者confirm模式
        //消息到达exchange的回调
        //implements RabbitTemplate.ConfirmCallback
// public void confirm(CorrelationData correlationData, boolean ack, String cause)
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause)->{
            if(ack){
                logger.info("消息{}接收成功",correlationData);
            }else{
                Message returnedMessage = correlationData.getReturnedMessage();
                SeckillMessage seckillMessage =
                        JsonUtil.jsonStr2Object(returnedMessage.getBody().toString(), SeckillMessage.class);
                redisTemplate.opsForValue().increment("seckillGoods:" + seckillMessage.getGoodsId(),1);
                //把生产者提交进队列失败的消息，存储在mysql表中
                // 等后期定时循环处理，或者人工手动处理
                SeckillError seckillError = new SeckillError();
                seckillError.setUserId(seckillMessage.getUser().getId());
                seckillError.setGoodsId(seckillMessage.getGoodsId());
                seckillErrorService.saveOrUpdate(seckillError);
                logger.info("消息{}接收失败，原因{}",correlationData,cause);
            }
        });
        //从exhcange路由到queue的回调
        //implements RabbitTemplate.ReturnCallback
// public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey)
        rabbitTemplate.setReturnCallback((msg, replyCode, replyText, exchange, routingKey)->{
            SeckillMessage seckillMessage =
                    JsonUtil.jsonStr2Object(msg.getBody().toString(), SeckillMessage.class);
            redisTemplate.opsForValue().increment("seckillGoods:" + seckillMessage.getGoodsId(),1);
            log.info("消息{}发送失败，应答码{}，原因{}，交换机{}，路由键{}",msg.toString(),replyCode,replyText,exchange,routingKey);
        });
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setMessageId(UUID.randomUUID().toString());
        Message message_AMQP = new Message(message.getBytes(StandardCharsets.UTF_8),messageProperties);
        logger.info("发送消息{}：{}" ,message_AMQP);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_SECKILL, "seckill.msg", message_AMQP);
    }
}
