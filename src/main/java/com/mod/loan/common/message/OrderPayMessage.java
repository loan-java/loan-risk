package com.mod.loan.common.message;

/**
 * 放款队列的DTO
 */
public class OrderPayMessage {

    private Long orderId; // 订单Id

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public OrderPayMessage(Long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "OrderPayMessage{" + "orderId='" + orderId + '}';
    }
}
