package com.mod.loan.util.pbUtil;

import com.alibaba.fastjson.JSON;
import com.mod.loan.util.pbUtil.dto.request.BaseRequest;
import com.mod.loan.util.pbUtil.dto.response.BaseResponse;
import com.mod.loan.util.pbUtil.dto.response.QueryRiskResultResponse;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
import com.mod.loan.util.pbUtil.http.HttpUtils;
import com.mod.loan.util.pbUtil.utils.constant.SlpConstant;
import com.mod.loan.util.pbUtil.utils.sign.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PanbaoClient {

    private static final Logger logger = LoggerFactory.getLogger(PanbaoClient.class);
    private String merchantId;
    private String merPriKey;
    private String platformPubKey;
    private String host;

    PanbaoClient(){

    }
    PanbaoClient(String merchantId, String merPriKey){
        this.merchantId = merchantId;
        this.merPriKey = merPriKey;
    }

    public RiskResultResponse creditRequest(BaseRequest request) {
        String rspStr = call(request, SlpConstant.creditApply);
        RiskResultResponse response = JSON.parseObject(rspStr, RiskResultResponse.class);
        verifySign(response);
        return response;
    }

    public QueryRiskResultResponse queryRequest(BaseRequest request) {
        String rspStr = call(request, SlpConstant.queryCreditResult);
        QueryRiskResultResponse response = JSON.parseObject(rspStr, QueryRiskResultResponse.class);
        verifySign(response);
        return response;
    }

    public BaseResponse pushRepayPlan(BaseRequest request) {
        String rspStr = call(request, SlpConstant.pushPlan);
        BaseResponse response = JSON.parseObject(rspStr, BaseResponse.class);
        verifySign(response);
        return response;
    }

    public BaseResponse pushRepayRecord(BaseRequest request) {
        String rspStr = call(request, SlpConstant.pushRecord);
        BaseResponse response = JSON.parseObject(rspStr, BaseResponse.class);
        verifySign(response);
        return response;
    }

    public BaseResponse pushRepayCollection(BaseRequest request) {
        String rspStr = call(request, SlpConstant.pushCollectionRecord);
        BaseResponse response = JSON.parseObject(rspStr, BaseResponse.class);
        verifySign(response);
        return response;
    }


    public String call(BaseRequest request, String requestUrl) {
        request.setMerchantId(merchantId);
        request.setSign(SignUtils.getSign(request, merPriKey));
        System.out.println("请求报文打印:"+ JSON.toJSONString(request));
        return HttpUtils.executePost(host+requestUrl, JSON.toJSONString(request));
    }

    private BaseResponse verifySign(BaseResponse response) {
        boolean b = SignUtils.verifySign(response, response.getSign(),platformPubKey);
        if (!b) {
            logger.error("验签失败");
        }
        return response;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerPriKey() {
        return merPriKey;
    }

    public void setMerPriKey(String merPriKey) {
        this.merPriKey = merPriKey;
    }

    public String getPlatformPubKey() {
        return platformPubKey;
    }

    public void setPlatformPubKey(String platformPubKey) {
        this.platformPubKey = platformPubKey;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}