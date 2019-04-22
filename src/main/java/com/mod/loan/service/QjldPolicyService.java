package com.mod.loan.service;

import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
public interface QjldPolicyService {

    DecisionResDetailDTO QjldPolicySync(String transId, User user, UserBank userBank);


    DecisionResDetailDTO QjldPolicyNoSync(String transId, User user, UserBank userBank);


    DecisionResDetailDTO QjldPolicQuery(String transId);
}
