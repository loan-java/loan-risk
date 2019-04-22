package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.OrderMapper;
import com.mod.loan.mapper.OrderPayMapper;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderPay;
import com.mod.loan.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends BaseServiceImpl<Order, Long> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderPayMapper orderPayMapper;

    @Override
    public void updatePayInfo(Order order, OrderPay orderPay) {
        if (order != null) {
            orderMapper.updateByPrimaryKeySelective(order);
        }
        orderPayMapper.insertSelective(orderPay);
    }

    @Override
    public void updatePayCallbackInfo(Order order, OrderPay orderPay) {
        orderMapper.updateByPrimaryKeySelective(order);
        orderPayMapper.updateByPrimaryKeySelective(orderPay);
    }

    @Override
    public void updateOrderByRisk(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

}
