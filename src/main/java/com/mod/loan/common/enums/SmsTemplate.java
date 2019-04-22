package com.mod.loan.common.enums;

public enum SmsTemplate {
	T1001("1001"), // 注册
	T1002("1002"), // 忘记密码

	T2001("2001"), // 打款
	T2002("2002"), // 当天提醒
	T2003("2003"),// 提前提醒
	;
	private String key; // 自有模板key

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private SmsTemplate(String key) {
		this.key = key;
	}

	public static SmsTemplate getTemplate(String key) {
		for (SmsTemplate enumYunpianApikey : SmsTemplate.values()) {
			if (enumYunpianApikey.getKey().equals(key)) {
				return enumYunpianApikey;
			}
		}
		return null;
	}
}
