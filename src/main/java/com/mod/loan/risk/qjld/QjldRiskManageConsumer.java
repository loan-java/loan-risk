package com.mod.loan.risk.qjld;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.message.QjldOrderIdMessage;
import com.mod.loan.common.message.RiskAuditMessage;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.config.rabbitmq.RabbitConst;
import com.mod.loan.config.redis.RedisConst;
import com.mod.loan.config.redis.RedisMapper;
import com.mod.loan.model.DTO.DecisionResDetailDTO;
import com.mod.loan.model.Merchant;
import com.mod.loan.model.Order;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.*;
import com.mod.loan.util.TimeUtils;
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

/**
 * loan-pay 2019/4/20 huijin.shuailijie Init
 */
@Component
public class QjldRiskManageConsumer {

    private static final Logger log = LoggerFactory.getLogger(QjldRiskManageConsumer.class);

    @Autowired
    private OrderService orderService;

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
    @Autowired
    private QjldPolicyService qjldPolicyService;
    @Autowired
    private QjldConfig qjldConfig;


    @RabbitListener(queues = "qjld_queue_risk_order_notify", containerFactory = "qjld_risk_order_notify")
    @RabbitHandler
    public void risk_order_notify(Message mess) {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        if (!redisMapper.lock(RedisConst.ORDER_LOCK + riskAuditMessage.getOrderId(), 30)) {
            log.error("风控查询消息重复，message={}", JSON.toJSONString(riskAuditMessage));
            return;
        }
        try {
            Order order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
            if (order == null) {
                log.info("风控查询，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            if (order.getStatus() != 11) { // 新建的订单才能进入风控模块
                log.info("风控查询，无效的订单状态 message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            Merchant merchant = merchantService.findMerchantByAlias(order.getMerchant());
            UserBank userBank = userBankService.selectUserCurrentBankCard(order.getUid());
            User user = userService.selectByPrimaryKey(order.getUid());
            String serials_no = String.format("%s%s%s", "p", new DateTime().toString(TimeUtils.dateformat5),
                    user.getId());
            DecisionResDetailDTO decisionResDetailDTO = qjldPolicyService.QjldPolicyNoSync(serials_no, user, userBank);
            rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_query_wait, new QjldOrderIdMessage(decisionResDetailDTO.getTrans_id(), riskAuditMessage.getOrderId()));
        } catch (Exception e) {

        }
    }

    @Bean("qjld_risk_order_notify")
    public SimpleRabbitListenerContainerFactory pointTaskContainerFactoryLoan(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(5);
        return factory;
    }
}
