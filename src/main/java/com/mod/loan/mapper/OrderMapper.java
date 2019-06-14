package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Order;

import java.util.List;

public interface OrderMapper extends MyBaseMapper<Order> {

    Order findUserLatestOrder(Long uid);

    /**
     * 查找用户历史订单
     */
    List<Order> getByUid(Long uid);

}