package com.mod.loan.service;

import com.mod.loan.common.enums.JuHeCallBackEnum;
import com.mod.loan.model.User;

/**
 * loan-risk 2019/4/25 huijin.shuailijie Init
 */
public interface CallBackJuHeService {

    void callBack(User user, String orderNo, JuHeCallBackEnum juHeCallBackEnum);
}
