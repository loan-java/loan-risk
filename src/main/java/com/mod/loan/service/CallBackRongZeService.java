package com.mod.loan.service;

import com.mod.loan.model.Order;

public interface CallBackRongZeService {

    /**
     * 推送订单状态
     * @param order
     */
    void pushOrderStatus(Order order);
}
