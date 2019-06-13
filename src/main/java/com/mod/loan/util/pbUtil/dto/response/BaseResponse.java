package com.mod.loan.util.pbUtil.dto.response;

import java.io.Serializable;

public class BaseResponse implements Serializable {

    private String rspCode;
    private String rspMsg;
    private String sign;

    public String getRspCode() {
        return rspCode;
    }

    public void setRspCode(String rspCode) {
        this.rspCode = rspCode;
    }

    public String getRspMsg() {
        return rspMsg;
    }

    public void setRspMsg(String rspMsg) {
        this.rspMsg = rspMsg;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
