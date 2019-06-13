package com.mod.loan.util.pbUtil.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public class RepaymentPlanBizContent {
    /**
     *还款计划列表
     */
    @NotEmpty
    List<BatchPaymentPlanDto> repayList;

    /*** 订单号 ***/
    @NotBlank
    private String loanNo;

    public List<BatchPaymentPlanDto> getRepayList() {
        return repayList;
    }

    public void setRepayList(List<BatchPaymentPlanDto> repayList) {
        this.repayList = repayList;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }
}
