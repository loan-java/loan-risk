package com.mod.loan.util.pbUtil.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RepaymentRecordBizContent {

    /*** 外部订单号 ***/
    @NotBlank
    private String loanNo;
    /**
     * 还款状态:0正常还款；1提前还款; 2逾期还款；结清4；
     */
    @NotNull
    private Integer repaymentStatus;

    /*** 期数 ***/
    @NotNull
    private Integer loanNumber;

    /*** 还款期次 ***/
    @NotNull
    private Integer loanRepaymentIndexs;

    /*** 计划还款时间 ***/
    @NotBlank
    private String repayDate;

    /*** 实际还款时间 ***/
    @NotBlank
    private String actualDate;

    /*** 应还总金额 ***/
    @NotBlank
    private String repayAmount;

    /*** 实还总金额 ***/
    @NotBlank
    private String actualAmount;

    /*** 本金 ***/
    @NotBlank
    private String capital;

    /*** 利息 ***/
    @NotBlank
    private String interest;

    /*** 贷后管理费 ***/
    private String postLoanManagementFee;

    /*** 逾期天数 ***/
    private String overdueDays;

    /*** 罚息 ***/
    @NotBlank
    private String penaltyAmount;

    /*** 滞纳金 ***/
    @NotBlank
    private String surchargeFee;

    /*** 结清时间,NULL 为未结清 ***/
    private String closeTime;

    /*** 交易期次订单号 ***/
    private String payOrderNo;

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public Integer getRepaymentStatus() {
        return repaymentStatus;
    }

    public void setRepaymentStatus(Integer repaymentStatus) {
        this.repaymentStatus = repaymentStatus;
    }

    public Integer getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(Integer loanNumber) {
        this.loanNumber = loanNumber;
    }

    public Integer getLoanRepaymentIndexs() {
        return loanRepaymentIndexs;
    }

    public void setLoanRepaymentIndexs(Integer loanRepaymentIndexs) {
        this.loanRepaymentIndexs = loanRepaymentIndexs;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
    }

    public String getActualDate() {
        return actualDate;
    }

    public void setActualDate(String actualDate) {
        this.actualDate = actualDate;
    }

    public String getRepayAmount() {
        return repayAmount;
    }

    public void setRepayAmount(String repayAmount) {
        this.repayAmount = repayAmount;
    }

    public String getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(String actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getPostLoanManagementFee() {
        return postLoanManagementFee;
    }

    public void setPostLoanManagementFee(String postLoanManagementFee) {
        this.postLoanManagementFee = postLoanManagementFee;
    }

    public String getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(String overdueDays) {
        this.overdueDays = overdueDays;
    }

    public String getPenaltyAmount() {
        return penaltyAmount;
    }

    public void setPenaltyAmount(String penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    public String getSurchargeFee() {
        return surchargeFee;
    }

    public void setSurchargeFee(String surchargeFee) {
        this.surchargeFee = surchargeFee;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getPayOrderNo() {
        return payOrderNo;
    }

    public void setPayOrderNo(String payOrderNo) {
        this.payOrderNo = payOrderNo;
    }
}
