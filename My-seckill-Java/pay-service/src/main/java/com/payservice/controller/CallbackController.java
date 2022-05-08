package com.payservice.controller;


import cn.felord.payment.wechat.v3.WechatApiProvider;
import cn.felord.payment.wechat.v3.WechatDirectPayApi;
import cn.felord.payment.wechat.v3.model.Amount;
import cn.felord.payment.wechat.v3.model.PayParams;
import cn.felord.payment.wechat.v3.model.ResponseSignVerifyParams;
import com.google.gson.Gson;
import comment.RespBean;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Profile({"wechat","dev"})
@RestController
@RequestMapping("/wxpay/notify")
public class CallbackController {
    private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

    private static final String TENANT_ID = "mobile";

    @Autowired
    private WechatApiProvider wechatApiProvider;
    @Autowired
    WechatDirectPayApi wechatDirectPayApi;

    /**
     * 微信支付成功回调.
     * <p>
     * 无需开发者判断，只有扣款成功微信才会回调此接口
     *
     * @param wechatpaySerial    the wechatpay serial
     * @param wechatpaySignature the wechatpay signature
     * @param wechatpayTimestamp the wechatpay timestamp
     * @param wechatpayNonce     the wechatpay nonce
     * @param request            the request
     * @return the map
     */
    @SneakyThrows
    @PostMapping("/native")
    public Map<String, ?> transactionCallback(
            @RequestHeader("Wechatpay-Serial") String wechatpaySerial,
            @RequestHeader("Wechatpay-Signature") String wechatpaySignature,
            @RequestHeader("Wechatpay-Timestamp") String wechatpayTimestamp,
            @RequestHeader("Wechatpay-Nonce") String wechatpayNonce,
            HttpServletRequest request,HttpServletResponse response) {
        String body = request.getReader().lines().collect(Collectors.joining());
        // 对请求头进行验签 以确保是微信服务器的调用
        ResponseSignVerifyParams params = new ResponseSignVerifyParams();
        params.setWechatpaySerial(wechatpaySerial);
        params.setWechatpaySignature(wechatpaySignature);
        params.setWechatpayTimestamp(wechatpayTimestamp);
        params.setWechatpayNonce(wechatpayNonce);
        params.setBody(body);
        // 对order表对应数据状态进行修改
        Gson gson = new Gson();
        Map<String, Object> bodyMap = gson.fromJson(body, HashMap.class);
        logger.info("支付通知的id ===> {}", bodyMap.get("id"));
        logger.info("支付通知的完整数据 ===> {}", body);

        return wechatApiProvider.callback(TENANT_ID).transactionCallback(params, data -> {
            //TODO 对回调解析的结果进行消费  需要保证消费的幂等性 微信有可能多次调用此接口
        });
    }
    /**
     * 对称解密
     * @param bodyMap
     * @return
     */
    private String decryptFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {
        logger.info("密文解密");
        //通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext = resourceMap.get("ciphertext");
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associatedData = resourceMap.get("associated_data");



        return "";
    }

}
