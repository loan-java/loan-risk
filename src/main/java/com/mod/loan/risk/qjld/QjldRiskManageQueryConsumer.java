package com.mod.loan.risk.qjld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.JuHeCallBackEnum;
import com.mod.loan.common.enums.OrderSourceEnum;
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

/**
 * loan-pay 2019/4/20 huijin.shuailijie Init
 */
@Component
public class QjldRiskManageQueryConsumer {

    private static final Logger log = LoggerFactory.getLogger(QjldRiskManageQueryConsumer.class);

    @Autowired
    private OrderService orderService;

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
    public void risk_order_notify(Message mess) {
        QjldOrderIdMessage qjldOrderIdMessage = JSONObject.parseObject(mess.getBody(), QjldOrderIdMessage.class);
        log.info("分控订单,[result]：" + qjldOrderIdMessage.toString());
        log.info("============================================================");
        Order order = orderService.selectByPrimaryKey(qjldOrderIdMessage.getOrderId());
        if (order == null) {
            log.info("风控订单，订单不存在 message={}", JSON.toJSONString(qjldOrderIdMessage));
            return;
        }
        if (order.getStatus() != ConstantUtils.newOrderStatus) { // 没有完成订单才能进入风控查询模块
            log.info("风控订单，订单已完成风控查询，message={}", JSON.toJSONString(qjldOrderIdMessage));
            return;
        }
        try {
            DecisionResDetailDTO decisionResDetailDTO = qjldPolicyService.qjldPolicQuery(qjldOrderIdMessage.getTransId());
            if (decisionResDetailDTO == null || OrderStatusEnum.INIT.getCode().equals(decisionResDetailDTO.getOrderStatus()) || OrderStatusEnum.WAIT.getCode().equals(decisionResDetailDTO.getOrderStatus())) {
                qjldOrderIdMessage.setTimes(qjldOrderIdMessage.getTimes() + 1);
                if (qjldOrderIdMessage.getTimes() < 6) {
                    rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_query_wait, qjldOrderIdMessage);
                    return;
                }
                rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_query_wait_long, qjldOrderIdMessage);
                return;
            }
            TbDecisionResDetail tbDecisionResDetail = new TbDecisionResDetail(decisionResDetailDTO);
            decisionResDetailService.updateByTransId(tbDecisionResDetail);
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
            if (order.getSource() == ConstantUtils.ONE) {
                callbackThird(order, decisionResDetailDTO);
            }
            log.info("分控订单,[result]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("风控订单查询异常{}", JSON.toJSONString(qjldOrderIdMessage));
            log.error("风控订单查询异常{}", e);
            if (qjldOrderIdMessage.getTimes() < 6) {
                qjldOrderIdMessage.setTimes(qjldOrderIdMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_query_wait, qjldOrderIdMessage);
                return;
            }
            order.setStatus(ConstantUtils.unsettledOrderStatus);
            orderService.updateOrderByRisk(order);
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

    private void callbackThird(Order order, DecisionResDetailDTO risk) {
        if (OrderSourceEnum.isRongZe(order.getSource())) {
            callBackRongZeService.pushRiskResult(order, risk.getCode(), risk.getDesc());
        }
    }
}
