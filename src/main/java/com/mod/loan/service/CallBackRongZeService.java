package com.mod.loan.service;

import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;

public interface CallBackRongZeService {

    /**
     * 推送风控审批结果
     *
     */
    void pushRiskResult(OrderUser orderUser, String riskCode, String riskDesc);
}
