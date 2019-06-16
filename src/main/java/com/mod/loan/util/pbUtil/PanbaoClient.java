package com.mod.loan.util.pbUtil;

import com.alibaba.fastjson.JSON;
import com.mod.loan.config.pb.PbConfig;
import com.mod.loan.util.pbUtil.dto.request.BaseRequest;
import com.mod.loan.util.pbUtil.dto.response.BaseResponse;
import com.mod.loan.util.pbUtil.dto.response.QueryRiskResultResponse;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
import com.mod.loan.util.pbUtil.http.HttpUtils;
import com.mod.loan.util.pbUtil.utils.constant.SlpConstant;
import com.mod.loan.util.pbUtil.utils.sign.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class PanbaoClient {

    @Autowired
    private PbConfig pbConfig;

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
        request.setMerchantId(pbConfig.getMerchantId());
        request.setSign(SignUtils.getSign(request, pbConfig.getPrivateKey()));
        log.info("请求报文打印");
        return HttpUtils.executePost(pbConfig.getPrevHost() + requestUrl, JSON.toJSONString(request));
    }

    private BaseResponse verifySign(BaseResponse response) {
        boolean b = SignUtils.verifySign(response, response.getSign(), pbConfig.getPublicKey());
        if (!b) {
            log.error("验签失败");
        }
        return response;
    }


}