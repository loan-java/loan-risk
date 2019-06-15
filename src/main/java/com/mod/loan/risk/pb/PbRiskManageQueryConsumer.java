package com.mod.loan.risk.pb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.message.RiskAuditMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;
import com.mod.loan.model.User;
import com.mod.loan.service.*;
import com.mod.loan.util.ConstantUtils;
import lombok.extern.slf4j.Slf4j;
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


    @RabbitListener(queues = "pb_queue_risk_order_result", containerFactory = "pb_risk_order_result")
    @RabbitHandler
    public void risk_order_query(Message mess) {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        log.info("分控订单,[result]：" + riskAuditMessage.toString());
        Order order = null;
        OrderUser orderUser = null;
        Long uid = null;
        String orderNo = null;
        if (riskAuditMessage.getSource() == ConstantUtils.ZERO && riskAuditMessage.getOrderId() != null) {
            order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
            if (order == null) {
                log.info("风控查询订单，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            if (order.getStatus() != ConstantUtils.newOrderStatus) { // 没有完成订单才能进入风控查询模块
                log.info("风控查询订单，订单已完成风控查询，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            uid = order.getUid();
            orderNo = order.getOrderNo();
        } else if (riskAuditMessage.getSource() == ConstantUtils.ONE && riskAuditMessage.getOrderNo() != null) {
            orderUser = orderUserService.selectByOrderNo(riskAuditMessage.getOrderNo());
            if (orderUser == null) {
                log.info("风控查询订单，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            uid = orderUser.getUid();
            orderNo = order.getOrderNo();
        } else {
            log.error("风控查询消息错误，message={}", JSON.toJSONString(riskAuditMessage));
            return;
        }
        try {
            User user = userService.selectByPrimaryKey(uid);
            //开始主动查询2.3接口
            DecisionPbDetail decisionPbDetail = decisionPbDetailService.creditApply(user, orderNo);
            if (decisionPbDetail == null) {
                throw new Exception("查询分控结果异常");
            }
            //聚合风控通过全部转为人工审核
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
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
                } else {
                    if (riskAuditMessage.getTimes() < 6) {
                        riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                        rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_result, riskAuditMessage);
                        return;
                    } else {
                        //超过次数人工处理
                        order.setStatus(ConstantUtils.unsettledOrderStatus);
                        orderService.updateOrderByRisk(order);
                    }
                }
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                callbackThird(orderUser, decisionPbDetail);
            }
            log.info("分控订单,[result]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("风控订单查询异常{}", JSON.toJSONString(riskAuditMessage));
            log.error("风控订单查询异常{}", e);
            if (riskAuditMessage.getTimes() < 6) {
                riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_result, riskAuditMessage);
                return;
            }
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                //聚合风控查询异常直接转入人工审核
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                //融泽风控查询异常直接返回审批失败 更新风控表
                DecisionPbDetail decisionPbDetail = new DecisionPbDetail();
                decisionPbDetail.setResult(PbResultEnum.DENY.getCode());
                decisionPbDetail.setDesc("拒绝");
                decisionPbDetail.setOrderNo(riskAuditMessage.getOrderNo());
                decisionPbDetail.setCreatetime(new Date());
                decisionPbDetail.setUpdatetime(new Date());
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
        callBackRongZeService.pushRiskResultForPb(orderUser, risk.getCode(), risk.getDesc());
    }
}
