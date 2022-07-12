package com.seckill.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: huangrendi
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelayCancelOrderMessage {

    private User user;

    private Long goodsId;

    private Long orderId;

    private Long seckillOrder;
}
