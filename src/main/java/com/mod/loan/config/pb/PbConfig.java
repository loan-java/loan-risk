package com.mod.loan.config.pb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class PbConfig {

    @Value("${pb.merchant.id}")
    private String merchantId;

    @Value("${pb.version}")
    private String version;

    @Value("${pb.product.id}")
    private String productId;

    @Value("${pb.private.key}")
    private String privateKey;

    @Value("${pb.public.key}")
    private String publicKey;


    @Value("${pb.prev.host}")
    private String prevHost;


    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrevHost() {
        return prevHost;
    }

    public void setPrevHost(String prevHost) {
        this.prevHost = prevHost;
    }

    @Override
    public String toString() {
        return "PbConfig{" +
                "merchantId='" + merchantId + '\'' +
                ", version='" + version + '\'' +
                ", productId='" + productId + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", prevHost='" + prevHost + '\'' +
                '}';
    }
}
