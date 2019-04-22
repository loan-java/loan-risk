package com.mod.loan.risk.qjld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.message.OrderPayMessage;
import com.mod.loan.common.message.QjldOrderIdMessage;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.enums.PolicyResultEnum;
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


    @RabbitListener(queues = "qjld_queue_risk_order_result", containerFactory = "qjld_risk_order_result")
    @RabbitHandler
    public void risk_order_notify(Message mess) {
        QjldOrderIdMessage qjldOrderIdMessage = JSONObject.parseObject(mess.getBody(), QjldOrderIdMessage.class);
        Order order = orderService.selectByPrimaryKey(qjldOrderIdMessage.getOrderId());
        if (order == null) {
            log.info("风控订单，订单不存在 message={}", JSON.toJSONString(qjldOrderIdMessage));
            return;
        }
        if (order.getStatus() != ConstantUtils.newOrderStatus) { // 没有完成订单才能进入风控查询模块
            log.info("风控订单，订单已完成风控查询，message={}", JSON.toJSONString(qjldOrderIdMessage));
            return;
        }
        DecisionResDetailDTO decisionResDetailDTO = qjldPolicyService.QjldPolicQuery(qjldOrderIdMessage.getTransId());
        if (decisionResDetailDTO == null) {
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
        if (PolicyResultEnum.AGREE.getCode().equals(decisionResDetailDTO.getCode())) {
            order.setStatus(ConstantUtils.agreeOrderStatus);
            orderService.updateOrderByRisk(order);
            rabbitTemplate.convertAndSend(RabbitConst.baofoo_queue_order_pay, new OrderPayMessage(order.getId()));
        } else if (PolicyResultEnum.UNSETTLED.getCode().equals(decisionResDetailDTO.getCode())) {
            order.setStatus(ConstantUtils.unsettledOrderStatus);
            orderService.updateOrderByRisk(order);
        } else {
            order.setStatus(ConstantUtils.rejectOrderStatus);
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
}
