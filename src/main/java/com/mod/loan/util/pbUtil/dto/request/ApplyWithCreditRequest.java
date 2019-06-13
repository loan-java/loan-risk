package com.mod.loan.util.pbUtil.dto.request;

import com.alibaba.fastjson.JSONObject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ApplyWithCreditRequest extends BaseRequest {
    /**
     * 模型号
     */
    @NotBlank
    private String version;

    /**
     * 用户姓名
     */
    @NotBlank
    private String userName;

    /**
     * 身份证号
     */
    @NotBlank
    private String cardNum;

    /**
     * 手机号
     */
    @NotBlank
    private String mobile;

    /**
     * 标识
     */
    private String tag;

    /**
     * 时间戳
     */
    @NotBlank
    private String timeStamp;

    /**
     * 进件数据
     */
    @NotNull
    private JSONObject riskData;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public JSONObject getRiskData() {
        return riskData;
    }

    public void setRiskData(JSONObject riskData) {
        this.riskData = riskData;
    }
}