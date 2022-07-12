package com.seckill.config;

import org.springframework.beans.factory.annotation.Configurable;


@Configurable
public class RabbitMQConfig {
    public static final String QUEUE_SECKILL = "seckillQueue";
    public static final String EXCHANGE_SECKILL = "seckillExchange";
    public static final String ROUTINGKEY_SECKILL="seckill.msg";
    // 延迟处理队列
    public static final String QUEUE_DELAY_SECKILL = "seckillDelayQueue";
    // 延迟ttl队列
    public static final String QUEUE_TTL_SECKILL = "seckillTTlQueue";

    public static final String EXCHANGE_DELAY_SECKILL = "seckillDelayExchange";

    public static final String ROUTINGKEY_DELAY_SECKILL ="seckillDelay.msg";


}
