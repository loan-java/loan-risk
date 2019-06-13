package com.mod.loan.util.pbUtil.dto.request;


import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class BaseRequest implements Serializable {

    private String merchantId;

    private String productId;

    /**
     * 业务类型:
     *      002、repay_plan还款计划上传
     *      003、repay_record还款记录上传
     */
    private String bizType;

    /**
     *  外部订单号
     */
    @NotBlank
    private String loanNo;

    /**
     *  拓展信息
     */
    private String other;

    /**
     * 十露盘版本号：默认1.0
     */
    private String slpVersion;

    /**
     * 编码方式：默认utf-8
     */
    private String charset;

    /**
     * 签名值
     */
    @NotBlank
    private String sign;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getSlpVersion() {
        return slpVersion;
    }

    public void setSlpVersion(String slpVersion) {
        this.slpVersion = slpVersion;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
