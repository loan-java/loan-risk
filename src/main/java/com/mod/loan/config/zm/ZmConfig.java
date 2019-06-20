package com.mod.loan.config.zm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ZmConfig {

    @Value("${zm.model.name}")
    private String modelName;

    @Value("${zm.url}")
    private String zmUrl;


    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getZmUrl() {
        return zmUrl;
    }

    public void setZmUrl(String zmUrl) {
        this.zmUrl = zmUrl;
    }
}
