package com.mod.loan.risk.pb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.message.RiskAuditMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PbRiskManageConsumer {

    @Autowired
    private OrderService orderService;


    @Autowired
    private MerchantService merchantService;
    @Autowired
    private UserService userService;
    @Autowired
    private DecisionPbDetailService decisionPbDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisMapper redisMapper;
    @Resource
    private CallBackRongZeService callBackRongZeService;

    @RabbitListener(queues = "pb_queue_risk_order_notify", containerFactory = "pb_risk_order_notify")
    @RabbitHandler
    public void risk_order_notify(Message mess) {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        Order order = null;
        Long uid = null;
        try {
            log.info("十露盘风控信息,[notify]：" + JSON.toJSONString(riskAuditMessage));
            //所有订单订单校验
            if (riskAuditMessage.getOrderId() != null) {
                order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
                if (!redisMapper.lock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId(), 30)) {
                    log.error("十露盘风控消息重复，message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order == null || order.getUid() == null || order.getOrderNo() == null) {
                    //third插入订单与查询是不同事务 防止queue取消息过快 查不到订单
                    TimeUnit.SECONDS.sleep(5L);
                    if (riskAuditMessage.getTimes() < 5) {
                        riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                        rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_notify, riskAuditMessage);
                        return;
                    }
                    log.error("十露盘风控，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order.getStatus() != 11) { // 新建的订单才能进入风控模块
                    log.error("十露盘风控，无效的订单状态 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                uid = order.getUid();
            } else {
                log.error("十露盘风控消息错误，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            Merchant merchant = merchantService.findMerchantByAlias(riskAuditMessage.getMerchant());
            if (merchant == null) {
                log.error("十露盘风控，无效的商户 message={}", riskAuditMessage.getMerchant());
                return;
            }
            if (!ConstantUtils.TWO.equals(merchant.getRiskType())) {
                log.error("十露盘风控，无效的风控类型 message={}", riskAuditMessage.getMerchant());
                return;
            }
            User user = userService.selectByPrimaryKey(uid);
            //开始请求2.2接口
            log.info("十露盘风控，开始请求" + order.getOrderNo());
            DecisionPbDetail decisionPbDetail = decisionPbDetailService.selectByOrderNo(order.getOrderNo());
            if (decisionPbDetail == null) {
                decisionPbDetail = decisionPbDetailService.creditApply(user, order);
            }
            if (decisionPbDetail != null && PbResultEnum.DENY.getCode().equals(decisionPbDetail.getResult()) && "拒绝".equals(decisionPbDetail.getDesc())) {
                //风控下单异常订单直接失败
                order.setStatus(ConstantUtils.rejectOrderStatus);
                orderService.updateOrderByRisk(order);
                if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                    callBackRongZeService.pushOrderStatus(order);
                }
                return;
            } else {
                if (decisionPbDetail == null) {
                    //风控下单异常订单直接失败
                    order.setStatus(ConstantUtils.rejectOrderStatus);
                    orderService.updateOrderByRisk(order);
                    if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                        callBackRongZeService.pushOrderStatus(order);
                    }
                    log.error("十露盘风控表数据新增失败，message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_result_wait, riskAuditMessage);
            }
            log.info("十露盘风控,[notify]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("十露盘风控异常{}", JSON.toJSONString(riskAuditMessage));
            log.error("十露盘风控异常", e);
            if (riskAuditMessage.getTimes() < 10) {
                riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_notify, riskAuditMessage);
                return;
            }
            try {
                //风控查询异常直接返回审批失败
                DecisionPbDetail decisionPbDetail = decisionPbDetailService.selectByOrderNo(order.getOrderNo());
                if (decisionPbDetail == null) {
                    decisionPbDetail = new DecisionPbDetail();
                    decisionPbDetail.setOrderId(order.getId());
                    decisionPbDetail.setResult(PbResultEnum.DENY.getCode());
                    decisionPbDetail.setDesc("拒绝");
                    decisionPbDetail.setOrderNo(riskAuditMessage.getOrderNo());
                    decisionPbDetail.setCreatetime(new Date());
                    decisionPbDetail.setUpdatetime(new Date());
                    decisionPbDetailService.insert(decisionPbDetail);

                }
                order.setStatus(ConstantUtils.rejectOrderStatus);
                orderService.updateOrderByRisk(order);
                if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                    callBackRongZeService.pushOrderStatus(order);
                }
            } catch (Exception e1) {
                log.error("十露盘风控异常2", e1);
            }
        } finally {
            redisMapper.unlock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId());
        }
    }

    @Bean("pb_risk_order_notify")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactoryLoan(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(5);
        return factory;
    }

}
