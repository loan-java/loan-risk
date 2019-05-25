package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.OrderUser;

public interface OrderUserService extends BaseService<OrderUser, Long> {

    OrderUser selectByOrderNo(String orderNo);
}
