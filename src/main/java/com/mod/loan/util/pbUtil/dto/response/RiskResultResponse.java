package com.mod.loan.util.pbUtil.dto.response;

public class RiskResultResponse extends BaseResponse{
    /**
     * 审核结果：“APPROVE”: 通过;“MANUAL”: 人工审核;“DENY”: 拒绝
     */
    private String result;
    /**
     * 模型评分
     */
    private String score;
    /**
     * 模型描述信息
     */
    private String desc;
    /**
     * 放款金额
     */
    private String loanAmount;
    /**
     * 放款利率
     */
    private String loanRate;
    /**
     * 放款期数
     */
    private Integer loanNumber;
    /**
     * 放款单位
     */
    private String loanUnit;


    /**
     * 十露盘唯一订单号
     */
    private String slpOrderNo;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(String loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLoanRate() {
        return loanRate;
    }

    public void setLoanRate(String loanRate) {
        this.loanRate = loanRate;
    }

    public Integer getLoanNumber() {
        return loanNumber;
    }

    public void setLoanNumber(Integer loanNumber) {
        this.loanNumber = loanNumber;
    }

    public String getLoanUnit() {
        return loanUnit;
    }

    public void setLoanUnit(String loanUnit) {
        this.loanUnit = loanUnit;
    }

    public String getSlpOrderNo() {
        return slpOrderNo;
    }

    public void setSlpOrderNo(String slpOrderNo) {
        this.slpOrderNo = slpOrderNo;
    }
}