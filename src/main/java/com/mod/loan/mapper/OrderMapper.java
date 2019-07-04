package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper extends MyBaseMapper<Order> {

    /**
     * 查找用户完成订单
     */
    List<Order> getDoubleLoanByUid(Long uid);


    List<Order> getOrderByUid(Long uid);

    Order findByOrderNoAndSource(@Param("orderNo") String orderNo, @Param("source") int source);
}