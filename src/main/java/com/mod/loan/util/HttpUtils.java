package com.mod.loan.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP 请求工具类
 */
public class HttpUtils {
    /**
     * 功能描述：获取真实的IP地址
     *
     * @param request
     */
    public static String getIpAddr(HttpServletRequest request, String split) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (!StringUtils.isEmpty(ip) && ip.contains(",")) {
            String[] ips = ip.split(",");
            ip = ips[ips.length - 1];
        }
        // 转换IP 格式
        if (!StringUtils.isEmpty(ip)) {
            ip = ip.replace(".", split).trim();
        }
        return ip;
    }
}