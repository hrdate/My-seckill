package com.seckill.config;
import com.google.common.collect.Maps;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

@Configurable
public class RabbitMQConfig {
    public static final String QUEUE_SECKILL = "seckillQueue";
    public static final String EXCHANGE_SECKILL = "seckillExchange";
    public static final String ROUTINGKEY_SECKILL="seckill.*";

    // 延迟处理队列
    public static final String QUEUE_DELAY_SECKILL = "seckillDelayQueue";
    // 延迟ttl队列
    public static final String QUEUE_TTL_SECKILL = "seckillTTlQueue";

    public static final String EXCHANGE_DELAY_SECKILL = "seckillDelayExchange";

    public static final String ROUTINGKEY_DELAY_SECKILL ="seckillDelay.*";

    @Bean(QUEUE_SECKILL)
    public Queue QUEUE_SECKILL(){
        /**
         * 1、name:    队列名称
         * 2、durable: 是否持久化
         * 3、exclusive: 是否独享、排外的。如果设置为true，定义为排他队列。则只有创建者可以使用此队列。也就是private私有的。
         * 4、autoDelete: 是否自动删除。也就是临时队列。当最后一个消费者断开连接后，会自动删除。
         * 默认 true，false，false
         * */
        return new Queue(QUEUE_SECKILL, true, false, false);
    }
    @Bean(EXCHANGE_SECKILL)
    public TopicExchange EXCHANGE_SECKILL(){
        //交换机,设置持久化durable=true
        return new TopicExchange(EXCHANGE_SECKILL,true,false);
    }

    @Bean
    public Binding BINDING_SECKILL(@Qualifier(QUEUE_SECKILL)Queue queue,@Qualifier(EXCHANGE_SECKILL)TopicExchange topicExchange){
        return BindingBuilder
                //绑定队列
                .bind(queue)
                //交换机
                .to(topicExchange)
                //设置匹配键
                .with(ROUTINGKEY_SECKILL);
    }

    /**
     *  延迟处理队列
     */
    @Bean(QUEUE_DELAY_SECKILL)
    public Queue QUEUE_DELAY_SECKILL(){
        return new Queue(QUEUE_DELAY_SECKILL, true, false, false);
    }

    /**
     * 延迟ttl队列
     */
    @Bean(QUEUE_TTL_SECKILL)
    public Queue QUEUE_TTL_SECKILL(){
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("x-dead-letter-exchange",RabbitMQConfig.EXCHANGE_DELAY_SECKILL);
        map.put("x-dead-letter-routing-key",RabbitMQConfig.QUEUE_DELAY_SECKILL);
        // 设置消息的TTl为30分钟
        map.put("x-message-ttl",1000 * 60 * 30);
        Queue queue = new Queue(QUEUE_TTL_SECKILL, true, false, false, map);
        return queue;
    }

    @Bean(EXCHANGE_DELAY_SECKILL)
    public TopicExchange EXCHANGE_DELAY_SECKILL(){
        //交换机,设置持久化durable=true
        return new TopicExchange(EXCHANGE_DELAY_SECKILL,true,false);
    }

    @Bean
    public Binding BINDING_DELAY_SECKILL(@Qualifier(QUEUE_DELAY_SECKILL)Queue queue,@Qualifier(EXCHANGE_DELAY_SECKILL)TopicExchange topicExchange){
        return BindingBuilder
                //绑定队列
                .bind(queue)
                //交换机
                .to(topicExchange)
                //设置匹配键
                .with(ROUTINGKEY_DELAY_SECKILL);
    }



}
