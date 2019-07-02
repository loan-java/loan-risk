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
    private DecisionPbDetailService decisionPbDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Resource
    private CallBackRongZeService callBackRongZeService;


    @RabbitListener(queues = "pb_queue_risk_order_result", containerFactory = "pb_risk_order_result")
    @RabbitHandler
    public void risk_order_query(Message mess) throws Exception {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        Order order = null;
        OrderUser orderUser = null;
        String orderNo = null;
        try {
            log.info("===============十露盘分控订单,[result]：" + JSON.toJSONString(riskAuditMessage));
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO && riskAuditMessage.getOrderId() != null) {
                order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
                if (order == null || order.getUid() == null || order.getOrderNo() == null) {
                    log.error("十露盘风控查询订单，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order.getStatus() != ConstantUtils.newOrderStatus) { // 没有完成订单才能进入风控查询模块
                    log.error("十露盘风控查询订单，订单已完成风控查询，message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                orderNo = order.getOrderNo();
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE && riskAuditMessage.getOrderNo() != null) {
                orderUser = orderUserService.selectByOrderNo(riskAuditMessage.getOrderNo());
                if (orderUser == null || orderUser.getUid() == null || orderUser.getOrderNo() == null) {
                    log.error("十露盘风控查询订单，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                orderNo = orderUser.getOrderNo();
            } else {
                log.error("十露盘风控查询消息错误，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            //开始主动查询2.3接口
            DecisionPbDetail decisionPbDetail = decisionPbDetailService.selectByOrderNo(orderNo);
            if(decisionPbDetail == null){
                log.error("十露盘风控表数据不存在[decisionPbDetail]，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            decisionPbDetail = decisionPbDetailService.queryCreditResult(decisionPbDetail);
            //聚合风控通过全部转为人工审核
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                if (PbResultEnum.APPROVE.getCode().equals(decisionPbDetail.getResult())) {
                    order.setStatus(ConstantUtils.agreeOrderStatus);
                    orderService.updateOrderByRisk(order);
                } else if (PbResultEnum.MANUAL.getCode().equals(decisionPbDetail.getResult())) {
                    order.setStatus(ConstantUtils.unsettledOrderStatus);
                    orderService.updateOrderByRisk(order);
                } else if (PbResultEnum.DENY.getCode().equals(decisionPbDetail.getResult())) {
                    order.setStatus(ConstantUtils.rejectOrderStatus);
                    orderService.updateOrderByRisk(order);
                } else {
                    if (riskAuditMessage.getTimes() < 6) {
                        riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                        rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_result_wait, riskAuditMessage);
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
            log.info("==============十露盘分控订单,[result]：结束==============");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("十露盘风控订单查询异常，相关信息{}", JSON.toJSONString(riskAuditMessage));
            log.error("十露盘风控订单查询异常信息", e);
            if (riskAuditMessage.getTimes() < 6) {
                riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_result_wait, riskAuditMessage);
                return;
            }
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                //聚合风控查询异常直接转入人工审核
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                try {
                    //融泽风控查询异常直接返回审批失败 更新风控表
                    DecisionPbDetail query = decisionPbDetailService.selectByOrderNo(riskAuditMessage.getOrderNo());
                    if(query == null){
                        log.error("十露盘风控表数据不存在[query]，message={}", JSON.toJSONString(riskAuditMessage));
                        return;
                    }
                    DecisionPbDetail decisionPbDetail = new DecisionPbDetail();
                    decisionPbDetail.setId(query.getId());
                    decisionPbDetail.setResult(PbResultEnum.DENY.getCode());
                    decisionPbDetail.setDesc("拒绝");
                    decisionPbDetail.setOrderNo(riskAuditMessage.getOrderNo());
                    decisionPbDetail.setUpdatetime(new Date());
                    decisionPbDetailService.updateByPrimaryKeySelective(decisionPbDetail);
                    callbackThird(orderUser, decisionPbDetail);
                }catch (Exception e1) {
                    log.error("十露盘风控订单查询异常信息[2]", e1);
                }
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
