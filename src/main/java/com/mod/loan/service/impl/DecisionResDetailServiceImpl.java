package com.mod.loan.service.impl;

import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.TbDecisionResDetailMapper;
import com.mod.loan.model.TbDecisionResDetail;
import com.mod.loan.service.DecisionResDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * loan-risk 2019/4/22 huijin.shuailijie Init
 */
@Service
public class DecisionResDetailServiceImpl extends BaseServiceImpl<TbDecisionResDetail, Long> implements DecisionResDetailService {
    @Autowired
    private TbDecisionResDetailMapper mapper;

    @Override
    public Integer updateByTransId(TbDecisionResDetail tbDecisionResDetail) {
        return mapper.updateByTransId(tbDecisionResDetail);
    }

    @Override
    public TbDecisionResDetail selectByTransId(String transId) {
        return mapper.selectByTransId(transId);
    }


}
