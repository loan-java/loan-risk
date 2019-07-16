package com.mod.loan.risk.qjld;

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
import com.mod.loan.model.*;
import com.mod.loan.service.*;
import com.mod.loan.util.ConstantUtils;
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

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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

    @RabbitListener(queues = "qjld_queue_risk_order_notify", containerFactory = "qjld_risk_order_notify")
    @RabbitHandler
    public void risk_order_notify(Message mess) {
        RiskAuditMessage riskAuditMessage = JSONObject.parseObject(mess.getBody(), RiskAuditMessage.class);
        Order order = null;
        try {
            log.info("新颜风控信息,[notify]：" + riskAuditMessage.toString());
            //订单校验
            if (riskAuditMessage.getOrderId() != null) {
                order = orderService.selectByPrimaryKey(riskAuditMessage.getOrderId());
                if (!redisMapper.lock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId(), 30)) {
                    log.error("新颜风控消息重复，message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order == null || order.getUid() == null || order.getOrderNo() == null) {
                    //third插入订单与查询是不同事务 防止queue取消息过快 查不到订单
                    TimeUnit.SECONDS.sleep(5L);
                    if (riskAuditMessage.getTimes() < 5) {
                        riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                        rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_notify, riskAuditMessage);
                        return;
                    }
                    log.error("新颜风控，订单不存在 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                if (order.getStatus() != 11) { // 新建的订单才能进入风控模块
                    log.error("新颜风控，无效的订单状态 message={}", JSON.toJSONString(riskAuditMessage));
                    return;
                }
                //融泽订单校验
            } else {
                log.error("新颜风控消息错误，message={}", JSON.toJSONString(riskAuditMessage));
                return;
            }
            Merchant merchant = merchantService.findMerchantByAlias(riskAuditMessage.getMerchant());
            if (merchant == null) {
                log.error("新颜风控，无效的商户 message={}", riskAuditMessage.getMerchant());
                return;
            }
            if (!ConstantUtils.ONE.equals(merchant.getRiskType())) {
                log.error("新颜风控，无效的风控类型 message={}", riskAuditMessage.getMerchant());
                return;
            }
            UserBank userBank = userBankService.selectUserCurrentBankCard(riskAuditMessage.getUid());
            User user = userService.selectByPrimaryKey(riskAuditMessage.getUid());
            String serials_no = String.format("%s%s%s", "p", new DateTime().toString(TimeUtils.dateformat5),
                    user.getId());
            DecisionResDetailDTO decisionResDetailDTO = qjldPolicyService.qjldPolicyNoSync(serials_no, user, userBank);
            if (decisionResDetailDTO != null) {
                TbDecisionResDetail tbDecisionResDetail = new TbDecisionResDetail(riskAuditMessage.getOrderId(), riskAuditMessage.getOrderNo(), decisionResDetailDTO.getDecision_no(), decisionResDetailDTO.getTrans_id(), decisionResDetailDTO.getOrderStatus());
                decisionResDetailService.insert(tbDecisionResDetail);
            }
            rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_result_wait, new QjldOrderIdMessage(decisionResDetailDTO.getTrans_id(), riskAuditMessage.getOrderId(), riskAuditMessage.getOrderNo(), riskAuditMessage.getSource()));
            log.info("新颜风控,[notify]：结束");
        } catch (Exception e) {
            //风控异常重新提交订单或者进入人工审核
            log.error("新颜风控异常{}", JSON.toJSONString(riskAuditMessage));
            log.error("新颜风控异常", e);
            if (riskAuditMessage.getTimes() < 10) {
                riskAuditMessage.setTimes(riskAuditMessage.getTimes() + 1);
                rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_notify, riskAuditMessage);
                return;
            }
            try {
                //风控下单异常直接返回审批失败 插入风控表
                DecisionResDetailDTO decisionRes = new DecisionResDetailDTO();
                decisionRes.setOrderStatus("FAIL");
                decisionRes.setCode(PolicyResultEnum.REJECT.getCode());
                decisionRes.setDesc("拒绝");
                TbDecisionResDetail resDetail = new TbDecisionResDetail(decisionRes);
                resDetail.setOrderNo(riskAuditMessage.getOrderNo());
                decisionResDetailService.insert(resDetail);
                order.setStatus(ConstantUtils.rejectOrderStatus);
                orderService.updateOrderByRisk(order);
                if (riskAuditMessage.getSource() == ConstantUtils.ONE) {
                    callBackRongZeService.pushOrderStatus(order);
                }
            } catch (Exception e1) {
                log.error("新颜风控异常2", e);
            }
        } finally {
            redisMapper.unlock(RedisConst.ORDER_POLICY_LOCK + riskAuditMessage.getOrderId());
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
