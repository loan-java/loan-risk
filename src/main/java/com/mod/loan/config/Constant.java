package com.mod.loan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Constant {

	public static String TEST;

	public static String ENVIROMENT;
	public static String SERVER_API_URL;
	public static String SERVER_H5_URL;
	public static String SERVER_ITF_URL;

	@Value("${test:}")
	public void setPICTURE_URL(String test) {
		TEST = test;
	}

	@Value("${environment:}")
	public void setENVIROMENT(String environment) {
		Constant.ENVIROMENT = environment;
	}

	@Value("${server.api.url:}")
	public void setServerApiUrl(String serverApiUrl) {
		SERVER_API_URL = serverApiUrl;
	}

	@Value("${server.h5.url:}")
	public void setServerH5Url(String serverH5Url) {
		SERVER_H5_URL = serverH5Url;
	}

	@Value("${server.itf.url:}")
	public void setServerItfUrl(String serverItfUrl) {
		SERVER_ITF_URL = serverItfUrl;
	}

}
