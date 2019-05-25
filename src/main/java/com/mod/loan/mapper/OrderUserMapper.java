package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.OrderUser;
import org.apache.ibatis.annotations.Param;

public interface OrderUserMapper extends MyBaseMapper<OrderUser> {

    OrderUser selectByOrderNo(@Param("orderNo")String orderNo);

}