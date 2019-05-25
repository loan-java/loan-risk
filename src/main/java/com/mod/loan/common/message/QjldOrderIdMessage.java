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

    /**
     * 平台订单编号
     */
    private String orderNo;

    /**
     * 订单来源，0-聚合，1-融泽
     */
    private Integer source;


    private int times;// 查询次数

    public QjldOrderIdMessage() {

    }

    public QjldOrderIdMessage(String transId, Long orderId, String orderNo, Integer source) {
        this.transId = transId;
        this.orderId = orderId;
        this.orderNo = orderNo;
        this.source = source;
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
                ", orderNo='" + orderNo + '\'' +
                ", source=" + source +
                ", times=" + times +
                '}';
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }
}
