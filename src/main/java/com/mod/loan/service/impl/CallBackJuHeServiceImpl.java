package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.JuHeCallBackEnum;
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
    public void callBack(User user, String orderNo, JuHeCallBackEnum juHeCallBackEnum) {
        log.info("回调订单信息:{}", orderNo);
        JSONObject object = JSONObject.parseObject(user.getCommonInfo());
        object.put("orderNo", orderNo);
        object.put("accountId", user.getId());
        object.put("orderType", juHeCallBackEnum.getOrderTypeEnum().getCode());
        object.put("orderStatus", juHeCallBackEnum.getOrderStatusEnum().getCode());
        object.put("payStatus", juHeCallBackEnum.getPayStatusEnum().getCode());
        object.put("repayStatus", juHeCallBackEnum.getRepayStatusEnum().getCode());

        CallBackJuHeUtil.callBack(juHeConfig.getJuHeCallBackUrl(), object);
    }
}
