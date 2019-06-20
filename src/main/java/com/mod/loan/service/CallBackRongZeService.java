package com.mod.loan.service;

import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;

public interface CallBackRongZeService {

    /**
     * 推送风控审批结果
     *
     */
    void pushRiskResultForQjld(OrderUser orderUser, String riskCode, String riskDesc);

    void pushRiskResultForPb(OrderUser orderUser, String riskCode, String riskDesc);

    void pushRiskResultForZm(OrderUser orderUser, String riskCode);
}
