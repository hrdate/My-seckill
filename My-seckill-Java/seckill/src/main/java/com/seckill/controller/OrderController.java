package com.seckill.controller;




import com.google.gson.Gson;
import com.seckill.entity.User;
import com.seckill.service.OrderService;
import com.seckill.vo.OrderDetailVo;
import comment.RespBean;
import comment.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

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
      * @param orderId
      * @return
      */
  @GetMapping("/detail")
  public RespBean detail(HttpServletRequest request,Long orderId){
      String userJson = request.getHeader("user");
      User user = new Gson().fromJson(userJson, User.class);
      if (null==user){
          return RespBean.error(RespBeanEnum.SESSION_ERROR);
      }
      OrderDetailVo detail = orderService.detail(orderId);
      return RespBean.success(detail);
  }

}
