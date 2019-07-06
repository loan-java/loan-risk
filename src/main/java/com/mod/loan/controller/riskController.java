package com.mod.loan.controller;

import com.mod.loan.common.enums.OrderSourceEnum;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.DecisionZmDetail;
import com.mod.loan.model.Order;
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
@RestController
public class riskController {

    @Autowired
    private DecisionPbDetailService decisionPbDetailService;
    @Autowired
    private DecisionZmDetailService zmDetailService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/creditApplyPb")
    public String creditApplyPb() throws Exception {
        User user =userService.selectByPrimaryKey((long)940);
        String orderNo="1665673124496871424";
        DecisionPbDetail decisionPbDetail = decisionPbDetailService.creditApply(user, orderService.findOrderByOrderNoAndSource(orderNo, OrderSourceEnum.RONGZE.getSoruce()));
        return String.valueOf(decisionPbDetail);
    }

    @RequestMapping(value = "/queryCreditResultPb")
    public String queryCreditResultPb() throws Exception {
        DecisionPbDetail detail = decisionPbDetailService.selectByPrimaryKey((long)282);
        DecisionPbDetail decisionPbDetail = decisionPbDetailService.queryCreditResult(detail);
        return String.valueOf(decisionPbDetail);
    }

    @RequestMapping(value = "/creditApplyZm")
    public String creditApplyZm() throws Exception {
        User user =userService.selectByPrimaryKey((long)940);
        String orderNo="1665673124496871424";
        Order order =orderService.findOrderByOrderNoAndSource(orderNo,OrderSourceEnum.RONGZE.getSoruce());
        DecisionZmDetail zmDetail = zmDetailService.creditApply(user, order);
        return String.valueOf(zmDetail);
    }


}
