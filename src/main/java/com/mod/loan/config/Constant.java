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

	public static String orgPrivateKey;

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

	@Value("${org.rsa.private.key:}")
	public void setOrgPrivateKey(String orgPrivateKey) {
		Constant.orgPrivateKey = orgPrivateKey;
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



}
