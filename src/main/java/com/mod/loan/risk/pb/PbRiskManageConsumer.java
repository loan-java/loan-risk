package com.mod.loan.risk.pb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.PolicyResultEnum;
import com.mod.loan.common.message.RiskAuditMessage;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.OrderUser;
import com.mod.loan.model.TbDecisionResDetail;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.CallBackRongZeService;
import com.mod.loan.service.DecisionPbDetailService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderService;
import com.mod.loan.service.OrderUserService;
import com.mod.loan.service.UserBankService;
import com.mod.loan.service.UserService;
import com.mod.loan.util.ConstantUtils;
import com.mod.loan.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisMapper redisMapper;

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
            //开始请求2.2接口


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
            //异常进入2.3接口查询分控结果
            rabbitTemplate.convertAndSend(RabbitConst.pb_queue_risk_order_query, riskAuditMessage);
            return;
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
