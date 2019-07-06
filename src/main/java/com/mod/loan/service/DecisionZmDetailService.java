package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.DecisionZmDetail;
import com.mod.loan.model.Order;
import com.mod.loan.model.User;

/**
 *
 */
public interface DecisionZmDetailService extends BaseService<DecisionZmDetail, Long> {

    DecisionZmDetail creditApply(User user, Order order ) throws Exception;

    DecisionZmDetail selectByOrderNo(String orderNo);

}
