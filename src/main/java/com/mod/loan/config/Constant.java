package com.mod.loan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Constant {

    public static String TEST;

    public static String ENVIROMENT;

    public static String rongZeRequestAppId;
    public static String rongZeCallbackUrl;
    public static String rongZeQueryUrl;
    public static String rongZePublicKey;
    public static String rongZeOrgPrivateKey;

    public static String bengBengRequestAppId;
    public static String bengBengCallbackUrl;
    public static String bengBengQueryUrl;
    public static String bengBengPublicKey;
    public static String bengBengOrgPrivateKey;


    @Value("${rongze.request.app.id:}")
    public void setRongZeRequestAppId(String rongZeRequestAppId) {
        Constant.rongZeRequestAppId = rongZeRequestAppId;
    }

    @Value("${rongze.callback.url:}")
    public void setRongZeCallbackUrl(String rongZeCallbackUrl) {
        Constant.rongZeCallbackUrl = rongZeCallbackUrl;
    }

    @Value("${rongze.query.url:}")
    public void setRongZeQueryUrl(String rongZeQueryUrl) {
        Constant.rongZeQueryUrl = rongZeQueryUrl;
    }

    @Value("${rongze.org.rsa.private.key:}")
    public void setOrgPrivateKey(String rongZeOrgPrivateKey) {
        Constant.rongZeOrgPrivateKey = rongZeOrgPrivateKey;
    }

    @Value("${rongze.rsa.public.key:}")
    public void setRongZePublicKey(String rongZePublicKey) {
        Constant.rongZePublicKey = rongZePublicKey;
    }

    @Value("${test:}")
    public void setPICTURE_URL(String test) {
        TEST = test;
    }

    @Value("${environment:}")
    public void setENVIROMENT(String environment) {
        Constant.ENVIROMENT = environment;
    }


    @Value("${bengbeng.request.app.id:}")
    public void setBengBengRequestAppId(String bengBengRequestAppId) {
        Constant.bengBengRequestAppId = bengBengRequestAppId;
    }

    @Value("${bengbeng.callback.url:}")
    public void setBengBengCallbackUrl(String bengBengCallbackUrl) {
        Constant.bengBengCallbackUrl = bengBengCallbackUrl;
    }

    @Value("${bengbeng.query.url:}")
    public void setBengBengQueryUrl(String bengBengQueryUrl) {
        Constant.bengBengQueryUrl = bengBengQueryUrl;
    }

    @Value("${bengbeng.rsa.public.key:}")
    public void setBengBengPublicKey(String bengBengPublicKey) {
        Constant.bengBengPublicKey = bengBengPublicKey;
    }

    @Value("${bengbeng.org.rsa.private.key:}")
    public void setBengBengOrgPrivateKey(String bengBengOrgPrivateKey) {
        Constant.bengBengOrgPrivateKey = bengBengOrgPrivateKey;
    }

}
