package com.mod.loan.config.rabbitmq;

public class RabbitConst {

    public final static String baofoo_queue_order_pay = "baofoo_queue_order_pay"; // 宝付放款队列


    public final static String kuaiqian_queue_order_pay = "kuaiqian_queue_order_pay"; // 快钱放款队列


    public final static String qjld_queue_risk_order_notify = "qjld_queue_risk_order_notify"; // 全景雷达风控订单审核通知
    public final static String qjld_queue_risk_order_result = "qjld_queue_risk_order_result"; // 全景雷达风控订单结果查询


    public final static String qjld_queue_risk_order_result_wait = "qjld_queue_risk_order_result_wait"; // 全景雷达风控订单结果等待5s

    public final static String qjld_queue_risk_order_result_wait_long = "qjld_queue_risk_order_result_wait_long"; // 全景雷达风控订单结果等待60S


    public final static String pb_queue_risk_order_notify = "pb_queue_risk_order_notify"; // 盘宝风控订单审核通知
    public final static String pb_queue_risk_order_result = "pb_queue_risk_order_result"; // 盘宝风控订单结果查询

    public final static String pb_queue_risk_order_result_wait = "pb_queue_risk_order_result_wait"; // 盘宝风控风控订单结果等待5s
    public final static String pb_queue_risk_order_result_wait_long = "pb_queue_risk_order_result_wait_long"; // 盘宝风控风控订单结果等待60s


}
