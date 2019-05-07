package com.mod.loan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Constant {

	public static String TEST;

	public static String ENVIROMENT;

	@Value("${test:}")
	public void setPICTURE_URL(String test) {
		TEST = test;
	}

	@Value("${environment:}")
	public void setENVIROMENT(String environment) {
		Constant.ENVIROMENT = environment;
	}



}
