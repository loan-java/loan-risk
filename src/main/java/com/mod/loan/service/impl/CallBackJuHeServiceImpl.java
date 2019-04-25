package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.JuHeOrderStatusEnum;
import com.mod.loan.common.enums.OrderTypeEnum;
import com.mod.loan.common.enums.PayStatusEnum;
import com.mod.loan.common.enums.RepayStatusEnum;
import com.mod.loan.config.juhe.JuHeConfig;
import com.mod.loan.model.User;
import com.mod.loan.service.CallBackJuHeService;
import com.mod.loan.util.CallBackJuHeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * loan-risk 2019/4/25 huijin.shuailijie Init
 */
@Slf4j
@Service
public class CallBackJuHeServiceImpl implements CallBackJuHeService {

    @Autowired
    private JuHeConfig juHeConfig;

    @Override
    public void callBack(User user, String orderNo) {
        log.info("回调订单信息:{}", orderNo);
        JSONObject object = JSONObject.parseObject(user.getCommonInfo());
        object.put("orderNo", orderNo);
        object.put("orderType", OrderTypeEnum.JK.getCode());
        object.put("accountId", user.getId());
        object.put("orderStatus", JuHeOrderStatusEnum.PAY_FAILED.getCode());
        object.put("payStatus", PayStatusEnum.NOTPAY.getCode());
        object.put("repayStatus", RepayStatusEnum.REPAY_FAILED.getCode());
        CallBackJuHeUtil.callBack(juHeConfig.getJuHeCallBackUrl(), object);
    }
}
