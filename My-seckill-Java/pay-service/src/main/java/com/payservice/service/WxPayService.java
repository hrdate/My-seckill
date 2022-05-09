package com.payservice.service;


import java.security.GeneralSecurityException;
import java.util.Map;

public interface WxPayService {
    Map<String, Object> nativePay(Long orderId) throws Exception;

    void processOrder(Map<String, Object> bodyMap) throws GeneralSecurityException;

    String queryOrder(String orderNo) throws Exception;

    String queryBill(String billDate, String type) throws Exception;

    String downloadBill(String billDate, String type) throws Exception;


}
