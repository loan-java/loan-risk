package com.mod.loan.util.pbUtil.dto.request;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class PushRepaymentRecordRequest extends BaseRequest{

    @NotEmpty
    List<RepaymentRecordBizContent> bizContent;

    public List<RepaymentRecordBizContent> getBizContent() {
        return bizContent;
    }

    public void setBizContent(List<RepaymentRecordBizContent> bizContent) {
        this.bizContent = bizContent;
    }
}