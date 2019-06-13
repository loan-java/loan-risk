package com.mod.loan.util.pbUtil.dto.request;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class BatchPaymentPlanDto {
    /*** 期数 ***/
    @NotNull
    private Integer loanNumber;

    /*** 期次 ***/
    @NotNull
    private Integer repaymentIndexs;

    /*** 计划开始时间 ***/
    @NotBlank
    private String valueDate;

    /*** 计划还款时间 ***/
    @NotBlank
    private String repayDate;

    /*** 应还款本金总额 ***/
    @NotBlank
    private String capital;

    /*** 应还款利息总额 ***/
    @NotBlank
    private String interest;

    /*** 借款利率 ***/
    @NotBlank
    private String loanRate;

    /*** 贷后管理费 ***/
    private String postLoanManagementFee;

    /*** 罚息费率 ***/
    private String penaltyInterestRate;

    /*** 罚息费用 ***/
    private String penaltyInterestFee;

    /*** 滞纳金比例 ***/
    private String surchargeRate;

    /*** 滞纳金金额 ***/
    private String surchargeFee;

    public Integer getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(Integer loanNumber) {
        this.loanNumber = loanNumber;
    }

    public Integer getRepaymentIndexs() {
        return repaymentIndexs;
    }

    public void setRepaymentIndexs(Integer repaymentIndexs) {
        this.repaymentIndexs = repaymentIndexs;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(String repayDate) {
        this.repayDate = repayDate;
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

    public String getLoanRate() {
        return loanRate;
    }

    public void setLoanRate(String loanRate) {
        this.loanRate = loanRate;
    }

    public String getPostLoanManagementFee() {
        return postLoanManagementFee;
    }

    public void setPostLoanManagementFee(String postLoanManagementFee) {
        this.postLoanManagementFee = postLoanManagementFee;
    }

    public String getPenaltyInterestRate() {
        return penaltyInterestRate;
    }

    public void setPenaltyInterestRate(String penaltyInterestRate) {
        this.penaltyInterestRate = penaltyInterestRate;
    }

    public String getPenaltyInterestFee() {
        return penaltyInterestFee;
    }

    public void setPenaltyInterestFee(String penaltyInterestFee) {
        this.penaltyInterestFee = penaltyInterestFee;
    }

    public String getSurchargeRate() {
        return surchargeRate;
    }

    public void setSurchargeRate(String surchargeRate) {
        this.surchargeRate = surchargeRate;
    }

    public String getSurchargeFee() {
        return surchargeFee;
    }

    public void setSurchargeFee(String surchargeFee) {
        this.surchargeFee = surchargeFee;
    }
}
