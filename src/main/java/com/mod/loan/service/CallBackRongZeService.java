package com.mod.loan.service;

import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.Order;

public interface CallBackRongZeService {

    /**
     * 推送订单状态
     *
     * @param order
     */
    void pushOrderStatus(Order order);

    /**
     * 推送风控审批结果
     *
     * @param order
     */
    void pushRiskResult(Order order, DecisionResDetailDTO risk);
}
