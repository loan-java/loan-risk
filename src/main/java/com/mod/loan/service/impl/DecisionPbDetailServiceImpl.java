package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.PbHeTianHelper;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.mapper.DecisionPbDetailMapper;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.service.DecisionPbDetailService;
import com.mod.loan.util.pbUtil.PanbaoClient;
import com.mod.loan.util.pbUtil.dto.request.ApplyWithCreditRequest;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DecisionPbDetailServiceImpl extends BaseServiceImpl<DecisionPbDetail, Long> implements DecisionPbDetailService {

    @Autowired
    private DecisionPbDetailMapper pbDetailMapper;

    @Autowired
    private PbHeTianHelper pbHeTianHelper;

    //2.2接口调用
    public DecisionPbDetail creditApply() {
        DecisionPbDetail decisionPbDetail =new DecisionPbDetail();

        return decisionPbDetail;
    }

    //2.3接口调用
    public DecisionPbDetail queryCreditResult() {
        DecisionPbDetail decisionPbDetail =new DecisionPbDetail();


        return decisionPbDetail;
    }


}