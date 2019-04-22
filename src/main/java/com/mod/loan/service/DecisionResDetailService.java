package com.mod.loan.service;

import com.mod.loan.common.mapper.BaseService;
import com.mod.loan.model.TbDecisionResDetail;

/**
 * loan-risk 2019/4/22 huijin.shuailijie Init
 */
public interface DecisionResDetailService extends BaseService<TbDecisionResDetail, Long> {
    Integer updateByTransId(TbDecisionResDetail tbDecisionResDetail);

    TbDecisionResDetail selectByTransId(String transId);
}
