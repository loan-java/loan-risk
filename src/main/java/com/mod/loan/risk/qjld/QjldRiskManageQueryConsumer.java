package com.mod.loan.risk.qjld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.JuHeCallBackEnum;
import com.mod.loan.common.enums.OrderStatusEnum;
import com.mod.loan.common.enums.PolicyResultEnum;
import com.mod.loan.common.message.QjldOrderIdMessage;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.Order;
import com.mod.loan.model.TbDecisionResDetail;
import com.mod.loan.service.*;
import com.mod.loan.util.ConstantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * loan-pay 2019/4/20 huijin.shuailijie Init
 */
@Component
public class QjldRiskManageQueryConsumer {

    private static final Logger log = LoggerFactory.getLogger(QjldRiskManageQueryConsumer.class);

    @Autowired
    private OrderService orderService;


    @Autowired
    private OrderUserService orderUserService;

    @Autowired
    private MerchantService merchantService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserBankService userBankService;

    @Autowired
    private DecisionResDetailService decisionResDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisMapper redisMapper;
    @Autowired
    private QjldPolicyService qjldPolicyService;
    @Autowired
    private QjldConfig qjldConfig;

    @Autowired
    private CallBackJuHeService callBackJuHeService;
    @Resource
    private CallBackRongZeService callBackRongZeService;


    @RabbitListener(queues = "qjld_queue_risk_order_result", containerFactory = "qjld_risk_order_result")
    @RabbitHandler
    public void risk_order_query(Message mess) {
        QjldOrderIdMessage qjldOrderIdMessage = JSONObject.parseObject(mess.getBody(), QjldOrderIdMessage.class);
        Order order = null;
        try {
            log.info("新颜风控订单,[result]：" + qjldOrderIdMessage.toString());
            if (qjldOrderIdMessage.getOrderId() != null) {
                order = orderService.selectByPrimaryKey(qjldOrderIdMessage.getOrderId());
                if (order == null) {
                    log.error("新颜风控查询订单，订单不存在 message={}", JSON.toJSONString(qjldOrderIdMessage));
                    return;
                }
                if (order.getStatus() != ConstantUtils.newOrderStatus) { // 没有完成订单才能进入风控查询模块
                    log.error("新颜风控查询订单，订单已完成风控查询，message={}", JSON.toJSONString(qjldOrderIdMessage));
                    return;
                }
            } else {
                log.error("新颜风控查询消息错误，message={}", JSON.toJSONString(qjldOrderIdMessage));
                return;
            }
            DecisionResDetailDTO decisionResDetailDTO = qjldPolicyService.qjldPolicQuery(qjldOrderIdMessage.getTransId());
            if (decisionResDetailDTO == null || OrderStatusEnum.INIT.getCode().equals(decisionResDetailDTO.getOrderStatus()) || OrderStatusEnum.WAIT.getCode().equals(decisionResDetailDTO.getOrderStatus())) {
                qjldOrderIdMessage.setTimes(qjldOrderIdMessage.getTimes() + 1);
                if (qjldOrderIdMessage.getTimes() < 6) {
                    rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_result_wait, qjldOrderIdMessage);
                    return;
                }
                rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_result_wait_long, qjldOrderIdMessage);
                return;
            }
            TbDecisionResDetail tbDecisionResDetail = new TbDecisionResDetail(decisionResDetailDTO);
            decisionResDetailService.updateByTransId(tbDecisionResDetail);

            if (PolicyResultEnum.AGREE.getCode().equals(decisionResDetailDTO.getCode())) {
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
                //支付类型为空的时候默认块钱的
                log.info("新颜放款类型:{}", order.getPaymentType());
            } else if (PolicyResultEnum.UNSETTLED.getCode().equals(decisionResDetailDTO.getCode())) {
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
            } else {
                order.setStatus(ConstantUtils.rejectOrderStatus);
                orderService.updateOrderByRisk(order);
                if (qjldOrderIdMessage.getSource() == ConstantUtils.ZERO) {
                    callBackJuHeService.callBack(userService.selectByPrimaryKey(order.getUid()), order.getOrderNo(), JuHeCallBackEnum.PAY_FAILED);
                }
            }
            if (qjldOrderIdMessage.getSource() == ConstantUtils.ONE) {
                callBackRongZeService.pushOrderStatus(order);
            }
            log.info("新颜分控订单,[result]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("新颜风控订单查询异常{}", JSON.toJSONString(qjldOrderIdMessage));
            log.error("新颜风控订单查询异常", e);
            if (qjldOrderIdMessage.getTimes() < 6) {
                qjldOrderIdMessage.setTimes(qjldOrderIdMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_result_wait, qjldOrderIdMessage);
                return;
            }
            try {
                if (qjldOrderIdMessage.getSource() == ConstantUtils.ZERO) {
                    //聚合风控查询异常直接转入人工审核
                    order.setStatus(ConstantUtils.unsettledOrderStatus);
                    orderService.updateOrderByRisk(order);
                } else if (qjldOrderIdMessage.getSource() == ConstantUtils.ONE) {
                    //融泽风控查询异常直接返回审批失败 更新风控表
                    DecisionResDetailDTO decisionRes = new DecisionResDetailDTO();
                    decisionRes.setTrans_id(qjldOrderIdMessage.getTransId());
                    decisionRes.setOrderStatus("FAIL");
                    decisionRes.setCode(PolicyResultEnum.REJECT.getCode());
                    decisionRes.setDesc("拒绝");
                    TbDecisionResDetail resDetail = new TbDecisionResDetail(decisionRes);
                    resDetail.setCreatetime(new Date());
                    decisionResDetailService.updateByTransId(resDetail);
                    order.setStatus(ConstantUtils.rejectOrderStatus);
                    orderService.updateOrderByRisk(order);
                    callBackRongZeService.pushOrderStatus(order);
                }
            } catch (Exception e1) {
                log.error("新颜风控订单查询异常2", e);
            }
        }
    }

    @Bean("qjld_risk_order_result")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactoryLoan(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(5);
        return factory;
    }

}
