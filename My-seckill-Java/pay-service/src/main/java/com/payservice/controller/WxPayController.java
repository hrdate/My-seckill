package com.payservice.controller;

import cn.felord.payment.wechat.v3.WechatApiProvider;
import cn.felord.payment.wechat.v3.WechatDirectPayApi;
import cn.felord.payment.wechat.v3.model.Amount;
import cn.felord.payment.wechat.v3.model.PayParams;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.payservice.service.OrderService;
import comment.RespBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
public class WxPayController {
    private static final Logger logger = LoggerFactory.getLogger(WxPayController.class);

    // 对应application-wechat.yml中的TENANT_ID字段为mobile的参数
    private static final String TENANT_ID = "mobile";
    // 请求回调地址
    private static final String NOTIFY_PATH = "/wxpay/notify/native";

    @Autowired
    private WechatApiProvider wechatApiProvider;
    @Autowired
    private OrderService OrderService;

    @GetMapping("/native/{orderId}")
    public RespBean nativePay(@PathVariable Long orderId) throws Exception {
        Amount amount = new Amount();
        amount.setTotal(1); // 订单总金额，单位为分
        amount.setCurrency("CNY"); // 货币类型,CNY:人民币
        PayParams payParams = new PayParams(); // 请求参数
        payParams.setDescription("微信支付测试"); // 商品描述
        payParams.setOutTradeNo(orderId.toString());
        payParams.setNotifyUrl(NOTIFY_PATH);
        WechatDirectPayApi wechatDirectPayApi = wechatApiProvider.directPayApi(TENANT_ID);
        ObjectNode body = wechatDirectPayApi.nativePay(payParams).getBody();
        String code_url = body.get("code_url").asText();

        return RespBean.success(code_url);
    }

}
