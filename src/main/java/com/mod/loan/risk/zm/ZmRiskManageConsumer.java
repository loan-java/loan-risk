package com.mod.loan.risk.zm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.message.RiskAuditMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.*;
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

@Slf4j
@Component
public class ZmRiskManageConsumer {

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
    private DecisionZmDetailService zmDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisMapper redisMapper;

    @Resource
    private CallBackRongZeService callBackRongZeService;

    @RabbitListener(queues = "zm_queue_risk_order_notify", containerFactory = "zm_risk_order_notify")
    @RabbitHandler
    public void risk_order_notify(Message mess) {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        Order order = null;
        OrderUser orderUser = null;
        Long uid = null;
        String orderNo = null;
        try {
            log.info("指迷风控信息,[notify]：" + JSON.toJSONString(riskAuditMessage));
            //聚合订单校验
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO && riskAuditMessage.getOrderId() != null) {
                order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
                if (!redisMapper.lock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId(), 30)) {
                    log.error("指迷风控消息重复，message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order == null || order.getUid() == null || order.getOrderNo() == null) {
                    log.info("指迷风控，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order.getStatus() != 11) { // 新建的订单才能进入风控模块
                    log.info("指迷风控，无效的订单状态 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                uid = order.getUid();
                orderNo = order.getOrderNo();
                //融泽订单校验
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE && riskAuditMessage.getOrderNo() != null) {
                orderUser = orderUserService.selectByOrderNo(riskAuditMessage.getOrderNo());
                if (!redisMapper.lock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderNo(), 30)) {
                    log.error("指迷风控消息重复，message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (orderUser == null || orderUser.getUid() == null || orderUser.getOrderNo() == null) {
                    log.info("指迷风控，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                uid = orderUser.getUid();
                orderNo = orderUser.getOrderNo();
            } else {
                log.error("指迷风控消息错误，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            Merchant merchant = merchantService.findMerchantByAlias(riskAuditMessage.getMerchant());
            if (merchant == null) {
                log.info("指迷风控，无效的商户 message={}", riskAuditMessage.getMerchant());
                return;
            }
            if (!ConstantUtils.THREE.equals(merchant.getRiskType())) {
                log.info("指迷风控，无效的风控类型 message={}", riskAuditMessage.getMerchant());
                return;
            }
            User user = userService.selectByPrimaryKey(uid);
            //开始请求2.2接口
            DecisionZmDetail zmDetail = zmDetailService.creditApply(user, orderNo);
            if(zmDetail == null){
                if (riskAuditMessage.getTimes() < 6) {
                    riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                    rabbitTemplate.convertAndSend(RabbitConst.zm_queue_risk_order_notify, riskAuditMessage);
                } else {
                    if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                        //聚合风控下单异常直接转入人工审核
                        order.setStatus(ConstantUtils.unsettledOrderStatus);
                        orderService.updateOrderByRisk(order);
                    } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                        //融泽风控查询异常直接返回审批失败
                        zmDetail = new DecisionZmDetail();
                        zmDetail.setReturnCode("-1");
                        zmDetail.setReturnInfo("fail");
                        zmDetail.setScore("0.0");
                        zmDetail.setOrderNo(riskAuditMessage.getOrderNo());
                        zmDetail.setCreatetime(new Date());
                        zmDetail.setUpdatetime(new Date());
                        zmDetailService.insert(zmDetail);
                        callbackThird(orderUser, zmDetail);
                    }
                }
            }else{
                if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                    if ( "-1".equals(zmDetail.getReturnCode())) {
                        order.setStatus(ConstantUtils.rejectOrderStatus);
                        orderService.updateOrderByRisk(order);

                    } else if ("0".equals(zmDetail.getReturnCode())) {
                        order.setStatus(ConstantUtils.agreeOrderStatus);
                        orderService.updateOrderByRisk(order);
                    }
                } else if(riskAuditMessage.getSource() == ConstantUtils.ONE){
                    //拒绝状态直接返回审批失败
                    callbackThird(orderUser, zmDetail);
                }
            }
            log.info("指迷风控,[notify]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("指迷风控异常{}", JSON.toJSONString(riskAuditMessage));
            log.error("指迷风控异常", e);
            if (riskAuditMessage.getTimes() < 6) {
                riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.zm_queue_risk_order_notify, riskAuditMessage);
            }else{
                if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                    //聚合风控下单异常直接转入人工审核
                    order.setStatus(ConstantUtils.unsettledOrderStatus);
                    orderService.updateOrderByRisk(order);
                } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                    //融泽风控查询异常直接返回审批失败
                    DecisionZmDetail zmDetail = new DecisionZmDetail();
                    zmDetail.setReturnCode("-1");
                    zmDetail.setReturnInfo("fail");
                    zmDetail.setScore("0.0");
                    zmDetail.setOrderNo(riskAuditMessage.getOrderNo());
                    zmDetail.setCreatetime(new Date());
                    zmDetail.setUpdatetime(new Date());
                    zmDetailService.insert(zmDetail);
                    callbackThird(orderUser, zmDetail);
                }
            }
        }finally {
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                redisMapper.unlock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId());
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                redisMapper.unlock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderNo());
            }
        }
    }

    @Bean("zm_risk_order_notify")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactoryLoan(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(5);
        return factory;
    }

    private void callbackThird(OrderUser orderUser, DecisionZmDetail risk) {
        callBackRongZeService.pushRiskResultForZm(orderUser, risk.getReturnCode());
    }
}
