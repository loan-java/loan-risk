package com.mod.loan.service;

import com.mod.loan.model.DTO.QjldHeTianResDTO;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;

/**
 * loan-risk 2019/4/29 huijin.shuailijie Init
 */
public interface QjldHeTianQueryService {

    QjldHeTianResDTO qjldHeTianQuery(String transId, User user, UserBank userBank);
}
