package com.mod.loan.util.pbUtil.dto.request;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class PushRepaymentPlanRequest extends BaseRequest{
    /**
     * 业务参数
     */
    @NotEmpty
    List<RepaymentPlanBizContent> bizContent;

    public List<RepaymentPlanBizContent> getBizContent() {
        return bizContent;
    }

    public void setBizContent(List<RepaymentPlanBizContent> bizContent) {
        this.bizContent = bizContent;
    }
}