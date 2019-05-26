package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.OrderUserMapper;
import com.mod.loan.model.OrderUser;
import com.mod.loan.service.OrderUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderUserServiceImpl extends BaseServiceImpl<OrderUser, Long> implements OrderUserService {

    @Autowired
    private OrderUserMapper orderUserMapper;

    @Override
    public OrderUser selectByOrderNo(String orderNo) {
        return orderUserMapper.selectByOrderNo(orderNo);
    }
}
