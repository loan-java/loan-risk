package com.mod.loan.common.message;

/**
 * 通知风控消息模型
 */
public class QjldOrderIdMessage {

    /**
     * 风控请求订单id
     */
    private String transId;

    /**
     * 平台订单ID
     */
    private Long orderId;

    private int times;// 查询次数

    public QjldOrderIdMessage(){

    }

    public QjldOrderIdMessage(String transId, Long orderId) {
        this.transId = transId;
        this.orderId = orderId;
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


    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    @Override
    public String toString() {
        return "QjldOrderIdMessage{" +
                "transId='" + transId + '\'' +
                ", orderId=" + orderId +
                ", times=" + times +
                '}';
    }
}
