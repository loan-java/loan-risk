package com.mod.loan.util.pbUtil.http;

import com.mod.loan.util.pbUtil.utils.gzip.GZipUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;

public class HttpUtils {

    private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    protected static CloseableHttpClient httpClient = HttpClientFactory
            .createHttpClient(300, 20, 30000, 3);


    public static String executePost(String url, String jsonStringParams) {
        byte[] bytes = GZipUtil.compressToByte(jsonStringParams);
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(bytes);
        byteArrayEntity.setContentEncoding("gzip");
        byteArrayEntity.setContentType("application/json");
        RequestBuilder requestBuilder = RequestBuilder
                .post()
                .setCharset(Charset.forName("UTF-8"))
                .setHeader("slp-content","gzip")
                .setHeader("Content-Type", "application/json")
                .setHeader("accept", "application/json")
                .setEntity(byteArrayEntity)
                .setUri(url);
        HttpUriRequest httpUriRequest = requestBuilder.build();
        CloseableHttpResponse responseDataEnc =execute(httpUriRequest);

        if(responseDataEnc != null){
            // 初始化: 解密结果
            int status = responseDataEnc.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = responseDataEnc.getEntity();
                try {
                    return EntityUtils.toString(entity, "utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if ((status >= 400 && status < 405) || status == 413){
                logger.info("Unexpected response status: " + status);
            }
        }
        return null;
    }

    public static CloseableHttpResponse execute(HttpUriRequest request) {
        try {
            return httpClient.execute(request, HttpClientContext.create());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}