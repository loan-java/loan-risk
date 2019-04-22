package com.mod.loan.common.enums;

/**
 * 
 * @author wugy 2018年1月9日 下午5:27:14
 */
public enum ResponseEnum {
	/**
	 * 2000开头为需要特殊处理的状态
	 */
    M2000("2000", "success"),
    
    /**
   	 * 4000开头为系统级别的异常
   	 */
    M4000("4000", "系统异常"),
    M4001("4001", "无效的版本号"),
    M4002("4002", "无效的TOKEN"),
    M4003("4003", "版本需要强制更新"),
	
    M5000("5000", "参数为空"),
    
    M70000("70000", "测试");

    private String code;

    private String message;

    ResponseEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
