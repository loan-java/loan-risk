package com.mod.loan.config.juhe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * loan-risk 2019/4/25 huijin.shuailijie Init
 */
@Component
public class JuHeConfig {

    @Value("${juhe.call.back.url:}")
    private String juHeCallBackUrl;


    public String getJuHeCallBackUrl() {
        return juHeCallBackUrl;
    }

    public void setJuHeCallBackUrl(String juHeCallBackUrl) {
        this.juHeCallBackUrl = juHeCallBackUrl;
    }
}
