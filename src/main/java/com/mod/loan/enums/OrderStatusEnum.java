package com.mod.loan.enums;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
public enum OrderStatusEnum {
    INIT("INIT", "初始化"),
    WAIT("WAIT", "处理中,请稍后查询"),
    SUCCESS("SUCCESS", "成功"),
    FAIL("FAIL", "失败");


    private final String code;
    private final String text;

    OrderStatusEnum(String code, String text) {
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
