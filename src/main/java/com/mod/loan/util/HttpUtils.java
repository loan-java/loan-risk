package com.mod.loan.util;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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


    /**
     * 发送rest风格请求
     *
     * @param URL    请求地址
     * @param data   包体内容
     * @param METHOD 请求方式
     * @return
     */
    public static String restRequest(final String URL, final String data, final String METHOD) {
        String result = null;
        HttpURLConnection conn = null;
        try {
            java.net.URL target = new URL(URL);
            conn = (HttpURLConnection) target.openConnection();
            conn.setRequestMethod(METHOD);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);// 是否输入参数
            byte[] bypes = data.getBytes();
            conn.getOutputStream().write(bypes);// 输入参数
            if (200 != conn.getResponseCode()) {
                throw new RuntimeException("failed, error code is " + conn.getResponseCode());
            }
            byte[] temp = new byte[conn.getInputStream().available()];
            if (conn.getInputStream().read(temp) != -1) {
                result = new String(temp, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return result;
    }
}