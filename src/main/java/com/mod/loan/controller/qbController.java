package com.mod.loan.controller;

import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.User;
import com.mod.loan.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
@Slf4j
@RequestMapping("/qb")
@RestController
public class qbController {

    @Autowired
    private DecisionPbDetailService decisionPbDetailService;


    @Autowired
    private UserService userService;

    @RequestMapping(value = "/creditApply")
    public String creditApply() throws Exception {
        User user =userService.selectByPrimaryKey((long)939);
        String orderNo="1661361583869370368";
        DecisionPbDetail decisionPbDetail = decisionPbDetailService.creditApply(user, orderNo);
        return decisionPbDetail.toString();
    }

    @RequestMapping(value = "/queryCreditResult")
    public String queryCreditResult() throws Exception {
        DecisionPbDetail detail = new DecisionPbDetail();
        DecisionPbDetail decisionPbDetail = decisionPbDetailService.queryCreditResult(detail);
        return decisionPbDetail.toString();
    }


}
