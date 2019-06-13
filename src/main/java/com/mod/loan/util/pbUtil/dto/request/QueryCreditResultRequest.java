package com.mod.loan.util.pbUtil.dto.request;

import javax.validation.constraints.NotBlank;

public class QueryCreditResultRequest extends BaseRequest{

    /**
     * 订单时间:yyyyMMdd 8位
     */
    @NotBlank
    private String orderDate;

    /**
     * 时间戳 yyyy-MM-dd HH:mm:ss
     */
    @NotBlank
    private String timeStamp;

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}