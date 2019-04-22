package com.mod.loan.common.message;

/**
 * 通知风控消息模型
 */
public class QjldOrderIdMessage {

    /**
     * 风控订单id
     */
    private String trans_id;

    /**
     * 平台订单ID
     */
    private Long orderId;

    private int times;// 查询次数

    public QjldOrderIdMessage(String trans_id, Long orderId) {
        this.trans_id = trans_id;
        this.orderId = orderId;
    }

    public String getTrans_id() {
        return trans_id;
    }

    public void setTrans_id(String trans_id) {
        this.trans_id = trans_id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }
}
