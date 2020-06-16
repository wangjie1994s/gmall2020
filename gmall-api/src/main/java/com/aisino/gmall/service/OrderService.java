package com.aisino.gmall.service;

import com.aisino.gmall.bean.OmsOrder;

public interface OrderService {
    String checkTradeCode(String memberId, String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);
}
