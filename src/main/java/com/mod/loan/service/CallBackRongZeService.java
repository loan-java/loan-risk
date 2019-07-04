package com.mod.loan.service;

import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;

public interface CallBackRongZeService {

    /**
     * 推送风控订单结果
     *
     */
    void pushOrderStatus(Order order) throws Exception;

}
