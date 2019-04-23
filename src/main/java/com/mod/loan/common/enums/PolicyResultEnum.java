package com.mod.loan.common.enums;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
public enum PolicyResultEnum {

    AGREE("AGREE", "通过"),
    REJECT("REJECT", "拒绝"),
    UNSETTLED("UNSETTLED", "人工审核"),
    ERROR("ERROR", "规则执行异常");


    private final String code;
    private final String text;

    PolicyResultEnum(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }
}
