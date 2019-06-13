package com.mod.loan.util.pbUtil.dto.response;

public class QueryRiskResultResponse extends RiskResultResponse {

    /**
     * 十露盘唯一订单号
     */
    private String slpOrderNo;
    /**
     * 商户方订单号
     */
    private String loanNo;

    /**
     * 订单状态
     */
    private String orderStatus;

    public String getSlpOrderNo() {
        return slpOrderNo;
    }

    public void setSlpOrderNo(String slpOrderNo) {
        this.slpOrderNo = slpOrderNo;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}