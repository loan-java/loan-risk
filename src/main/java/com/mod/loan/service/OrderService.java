package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderPay;

public interface OrderService extends BaseService<Order, Long> {

	void updatePayInfo(Order order, OrderPay orderPay);

	void updatePayCallbackInfo(Order order, OrderPay orderPay);

	void updateOrderByRisk(Order order);

	/**
	 * 根据订单编号查订单
	 * @param orderNo
	 * @return
	 */
	Order findOrderByOrderNoAndSource(String orderNo, int source);
}
