package com.seckill.rabbitmq;


import com.seckill.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@Slf4j
public class MQSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    private static Logger logger =   LoggerFactory.getLogger(MQSender.class);

    public void sendsecKillMessage(String message) {
        //生产者confirm模式
        //消息到达exchange的回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause)->{
            if(ack){
                logger.info("消息{}接收成功",correlationData.getId());
            }else{
                logger.info("消息{}接收失败，原因{}",correlationData.getId(),cause);
            }
        });
        //从exhcange路由到queue的回调
        rabbitTemplate.setReturnCallback((msg, replyCode, replyText, exchange, routingKey)->{
            log.info("消息{}发送失败，应答码{}，原因{}，交换机{}，路由键{}",msg.toString(),replyCode,replyText,exchange,routingKey);
        });
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        logger.info("发送消息{}：{}" ,correlationData.getId(), message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_SECKILL, "seckill.msg", message,correlationData);
    }
}
