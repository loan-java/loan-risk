package com.mod.loan.service;

import com.mod.loan.model.Order;

public interface CallBackRongZeService {

    /**
     * 推送风控订单结果
     *
     */
    void pushOrderStatus(Order order) throws Exception;

}
