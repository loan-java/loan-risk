package com.mod.loan.common.enums;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
public enum ExamineStatusEnum {

    PASS("PASS", "审核通过"),
    NO_PASS("NO_PASS", "审核不通过");

    private final String code;
    private final String text;

    ExamineStatusEnum(String code, String text) {
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
