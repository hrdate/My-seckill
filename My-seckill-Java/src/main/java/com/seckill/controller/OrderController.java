package com.seckill.controller;


import com.seckill.comment.RespBean;
import com.seckill.comment.RespBeanEnum;
import com.seckill.entity.User;
import com.seckill.service.OrderService;
import com.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    /**
      * 订单详情
      * @param user
      * @param orderId
      * @return
      */
  @RequestMapping("/detail")
  @ResponseBody
  public RespBean detail(User user, Long orderId){
   if (null==user){
     return RespBean.error(RespBeanEnum.SESSION_ERROR);
   }
   OrderDetailVo detail = orderService.detail(orderId);
   return RespBean.success(detail);
  }

}
