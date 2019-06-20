package com.mod.loan.controller;

import com.mod.loan.common.enums.JuHeCallBackEnum;
import com.mod.loan.common.enums.PolicyResultEnum;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.DTO.EngineResult;
import com.mod.loan.model.DTO.ManualAuditDTO;
import com.mod.loan.model.Order;
import com.mod.loan.model.TbDecisionResDetail;
import com.mod.loan.service.*;
import com.mod.loan.util.ConstantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
@Slf4j
@RequestMapping("/qjldCallBack")
@RestController
public class QjldCallBackController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private DecisionResDetailService decisionResDetailService;
    @Autowired
    private DecisionZmDetailService zmDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private CallBackJuHeService callBackJuHeService;

    @Autowired
    private QjldConfig qjldConfig;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/policyCallBack", method = RequestMethod.POST)
    public String QjldPolicyCallBack(@RequestBody EngineResult<DecisionResDetailDTO> engineResult) {
        log.info("全景雷达异步查询回调接口");
        log.info("data数据：" + engineResult.getData().toString());
        log.info("============================================================");
        if (engineResult == null || engineResult.getData() == null) {
            return ConstantUtils.FAIL;
        }
        DecisionResDetailDTO decisionResDetailDTO = engineResult.getData();
        TbDecisionResDetail tbDecisionResDetail = decisionResDetailService.selectByTransId(decisionResDetailDTO.getTrans_id());
        Order order = orderService.selectByPrimaryKey(tbDecisionResDetail.getOrderId());
        if (order == null) {
            log.info("风控订单，订单不存在");
            return ConstantUtils.FAIL;

        }
        if (order.getStatus() != ConstantUtils.newOrderStatus) { // 新建订单才能进入风控查询模块
            log.info("风控订单，订单状态异常");
            return ConstantUtils.FAIL;
        }
        if (tbDecisionResDetail != null) {
            TbDecisionResDetail updateDetail = new TbDecisionResDetail(decisionResDetailDTO);
            decisionResDetailService.updateByTransId(updateDetail);
            //风控通过全部转为人工审核
            if (PolicyResultEnum.AGREE.getCode().equals(decisionResDetailDTO.getCode())) {
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
                //支付类型为空的时候默认块钱的
                log.info("放款类型：" + order.getPaymentType());
            } else if (PolicyResultEnum.UNSETTLED.getCode().equals(decisionResDetailDTO.getCode())) {
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
            } else {
                order.setStatus(ConstantUtils.rejectOrderStatus);
                orderService.updateOrderByRisk(order);
                callBackJuHeService.callBack(userService.selectByPrimaryKey(order.getUid()), order.getOrderNo(), JuHeCallBackEnum.PAY_FAILED);
            }
            log.info("全景雷达异步查询回调接口：成功结束");
            return ConstantUtils.OK;
        }
        log.info("全景雷达异步查询回调接口：失败结束");
        return ConstantUtils.FAIL;
    }

    @RequestMapping(value = "/manualAuditCallBack", method = RequestMethod.POST)
    public String QjldManualAuditCallBack(@RequestBody ManualAuditDTO manualAuditDTO) {
        System.out.println(manualAuditDTO.toString());
        return ConstantUtils.OK;
    }


}
