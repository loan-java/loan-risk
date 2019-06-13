package com.mod.loan.risk.pb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.PolicyResultEnum;
import com.mod.loan.common.message.QjldOrderIdMessage;
import com.mod.loan.common.message.RiskAuditMessage;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;
import com.mod.loan.model.TbDecisionResDetail;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.CallBackRongZeService;
import com.mod.loan.service.DecisionResDetailService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderService;
import com.mod.loan.service.OrderUserService;
import com.mod.loan.service.QjldPolicyService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.ConstantUtils;
import com.mod.loan.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
@Slf4j
@Component
public class PbRiskManageConsumer {

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

    @Resource
    private CallBackRongZeService callBackRongZeService;

    @RabbitListener(queues = "pb_queue_risk_order_notify", containerFactory = "pb_risk_order_notify")
    @RabbitHandler
    public void risk_order_notify(Message mess) {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        log.info("风控信息,[notify]：" + riskAuditMessage.toString());
        Order order = null;
        OrderUser orderUser = null;
        //聚合订单校验
        if (riskAuditMessage.getSource() == ConstantUtils.ZERO && riskAuditMessage.getOrderId() != null) {
            order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
            if (!redisMapper.lock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId(), 30)) {
                log.error("风控消息重复，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            if (order == null) {
                log.info("风控，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            if (order.getStatus() != 11) { // 新建的订单才能进入风控模块
                log.info("风控，无效的订单状态 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            //融泽订单校验
        } else if (riskAuditMessage.getSource() == ConstantUtils.ONE && riskAuditMessage.getOrderNo() != null) {
            orderUser = orderUserService.selectByOrderNo(riskAuditMessage.getOrderNo());
            if (!redisMapper.lock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderNo(), 30)) {
                log.error("风控消息重复，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            if (orderUser == null) {
                log.info("风控，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
        } else {
            log.error("风控消息错误，message={}", JSON.toJSONString(riskAuditMessage));
            return;
        }
        Merchant merchant = merchantService.findMerchantByAlias(riskAuditMessage.getMerchant());
        if (merchant == null) {
            log.info("风控，无效的商户 message={}", riskAuditMessage.getMerchant());
            return;
        }
        try {
            UserBank userBank = userBankService.selectUserCurrentBankCard(riskAuditMessage.getUid());
            User user = userService.selectByPrimaryKey(riskAuditMessage.getUid());
            String serials_no = String.format("%s%s%s", "p", new DateTime().toString(TimeUtils.dateformat5),
                    user.getId());
            DecisionResDetailDTO decisionResDetailDTO = qjldPolicyService.qjldPolicyNoSync(serials_no, user, userBank);
            if (decisionResDetailDTO != null) {
                TbDecisionResDetail tbDecisionResDetail = new TbDecisionResDetail(riskAuditMessage.getOrderId(), riskAuditMessage.getOrderNo(), decisionResDetailDTO.getDecision_no(), decisionResDetailDTO.getTrans_id(), decisionResDetailDTO.getOrderStatus());
                decisionResDetailService.insert(tbDecisionResDetail);
            }
            rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_query_wait, new QjldOrderIdMessage(decisionResDetailDTO.getTrans_id(), riskAuditMessage.getOrderId(), riskAuditMessage.getOrderNo(), riskAuditMessage.getSource()));
            log.info("风控,[notify]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("风控异常{}", JSON.toJSONString(riskAuditMessage));
            log.error("风控异常", e);
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                redisMapper.unlock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId());
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                redisMapper.unlock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderNo());
            }
            if (riskAuditMessage.getTimes() < 6) {
                riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_notify, riskAuditMessage);
                return;
            }
            if (riskAuditMessage.getSource() == ConstantUtils.ZERO) {
                //聚合风控下单异常直接转入人工审核
                order.setStatus(ConstantUtils.unsettledOrderStatus);
                orderService.updateOrderByRisk(order);
            } else if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                //融泽风控下单异常直接返回审批失败 插入风控表
                DecisionResDetailDTO decisionRes = new DecisionResDetailDTO();
                decisionRes.setOrderStatus("FAIL");
                decisionRes.setCode(PolicyResultEnum.REJECT.getCode());
                decisionRes.setDesc("拒绝");
                TbDecisionResDetail resDetail = new TbDecisionResDetail(decisionRes);
                resDetail.setOrderNo(riskAuditMessage.getOrderNo());
                decisionResDetailService.insert(resDetail);
                callbackThird(orderUser, decisionRes);
            }
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

    private void callbackThird(OrderUser orderUser, DecisionResDetailDTO risk) {
        callBackRongZeService.pushRiskResult(orderUser, risk.getCode(), risk.getDesc());
    }
}
