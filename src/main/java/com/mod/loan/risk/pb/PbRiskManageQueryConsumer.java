package com.mod.loan.risk.pb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.JuHeCallBackEnum;
import com.mod.loan.common.enums.OrderStatusEnum;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.enums.PolicyResultEnum;
import com.mod.loan.common.message.QjldOrderIdMessage;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;
import com.mod.loan.model.TbDecisionResDetail;
import com.mod.loan.service.CallBackJuHeService;
import com.mod.loan.service.CallBackRongZeService;
import com.mod.loan.service.DecisionPbDetailService;
import com.mod.loan.service.DecisionResDetailService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderService;
import com.mod.loan.service.OrderUserService;
import com.mod.loan.service.QjldPolicyService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.ConstantUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class PbRiskManageQueryConsumer {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderUserService orderUserService;
    @Autowired
    private UserService userService;
    @Autowired
    private DecisionPbDetailService decisionPbDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CallBackJuHeService callBackJuHeService;
    @Resource
    private CallBackRongZeService callBackRongZeService;


    @RabbitListener(queues = "pb_queue_risk_order_query", containerFactory = "pb_risk_order_result")
    @RabbitHandler
    public void risk_order_query(Message mess) {
        QjldOrderIdMessage qjldOrderIdMessage = JSONObject.parseObject(mess.getBody(), QjldOrderIdMessage.class);
        log.info("分控订单,[result]：" + qjldOrderIdMessage.toString());
        Order order = null;
        OrderUser orderUser = null;
        if (qjldOrderIdMessage.getSource() == ConstantUtils.ZERO && qjldOrderIdMessage.getOrderId() != null) {
            order = orderService.selectByPrimaryKey(qjldOrderIdMessage.getOrderId());
            if (order == null) {
                log.info("风控查询订单，订单不存在 message={}", JSON.toJSONString(qjldOrderIdMessage));
                return;
            }
            if (order.getStatus() != ConstantUtils.newOrderStatus) { // 没有完成订单才能进入风控查询模块
                log.info("风控查询订单，订单已完成风控查询，message={}", JSON.toJSONString(qjldOrderIdMessage));
                return;
            }
        } else if (qjldOrderIdMessage.getSource() == ConstantUtils.ONE && qjldOrderIdMessage.getOrderNo() != null) {
            orderUser = orderUserService.selectByOrderNo(qjldOrderIdMessage.getOrderNo());
            if (orderUser == null) {
                log.info("风控查询订单，订单不存在 message={}", JSON.toJSONString(qjldOrderIdMessage));
                return;
            }
        } else {
            log.error("风控查询消息错误，message={}", JSON.toJSONString(qjldOrderIdMessage));
            return;
        }
        try {
            //开始主动查询2.3接口
            DecisionPbDetail decisionPbDetail = new DecisionPbDetail();

            //聚合风控通过全部转为人工审核
            if (qjldOrderIdMessage.getSource() == ConstantUtils.ZERO) {
                if (PbResultEnum.APPROVE.getCode().equals(decisionPbDetail.getResult())) {
                    order.setStatus(ConstantUtils.agreeOrderStatus);
                    orderService.updateOrderByRisk(order);
                    //支付类型为空的时候默认块钱的
                    log.info("放款类型：" + order.getPaymentType());
                    //目前都是人工放款
                } else if (PbResultEnum.MANUAL.getCode().equals(decisionPbDetail.getResult())) {
                    order.setStatus(ConstantUtils.unsettledOrderStatus);
                    orderService.updateOrderByRisk(order);
                } else if (PbResultEnum.DENY.getCode().equals(decisionPbDetail.getResult())) {
                    order.setStatus(ConstantUtils.rejectOrderStatus);
                    orderService.updateOrderByRisk(order);
                }else {
                    if (qjldOrderIdMessage.getTimes() < 6) {
                        qjldOrderIdMessage.setTimes(qjldOrderIdMessage.getTimes() + 1);
                        rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_query, qjldOrderIdMessage);
                        return;
                    }else{
                        //超过次数人工处理
                        order.setStatus(ConstantUtils.unsettledOrderStatus);
                        orderService.updateOrderByRisk(order);
                    }
                }
            } else if (qjldOrderIdMessage.getSource() == ConstantUtils.ONE) {
                callbackThird(orderUser, decisionPbDetail);
            }
            log.info("分控订单,[result]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("风控订单查询异常{}", JSON.toJSONString(qjldOrderIdMessage));
            log.error("风控订单查询异常{}", e);
            if (qjldOrderIdMessage.getTimes() < 6) {
                qjldOrderIdMessage.setTimes(qjldOrderIdMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_query, qjldOrderIdMessage);
                return;
            }
            if (qjldOrderIdMessage.getSource() == ConstantUtils.ZERO) {
                //聚合风控查询异常直接转入人工审核
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
            } else if (qjldOrderIdMessage.getSource() == ConstantUtils.ONE) {
                //融泽风控查询异常直接返回审批失败 更新风控表
                DecisionPbDetail decisionPbDetail = new DecisionPbDetail();
                decisionPbDetail.setStatus("FAIL");
                decisionPbDetail.setCode(PolicyResultEnum.REJECT.getCode());
                decisionPbDetail.setDesc("拒绝");
                decisionPbDetail.setOrderNo(qjldOrderIdMessage.getOrderNo());
                decisionPbDetailService.insert(decisionPbDetail);
                callbackThird(orderUser, decisionPbDetail);
            }
        }
    }

    @Bean("pb_risk_order_result")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactoryLoan(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(5);
        return factory;
    }

    private void callbackThird(OrderUser orderUser, DecisionPbDetail risk) {
        callBackRongZeService.pushRiskResult(orderUser, risk.getCode(), risk.getDesc());
    }
}
