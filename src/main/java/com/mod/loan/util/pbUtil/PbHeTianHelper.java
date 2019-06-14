//package com.mod.loan.util.pbUtil;
//
//import com.alibaba.fastjson.JSON;
//import com.mod.loan.config.Constant;
//import com.mod.loan.config.pb.PbConfig;
//import com.mod.loan.util.pbUtil.PanbaoClient;
//import com.mod.loan.util.pbUtil.dto.request.ApplyWithCreditRequest;
//import com.mod.loan.util.pbUtil.dto.request.QueryCreditResultRequest;
//import com.mod.loan.util.pbUtil.dto.response.BaseResponse;
//import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PostConstruct;
//
///**
// * 决策请求工具类
// *
// * @author lijing
// * @date 2017/12/26 0026.
// */
//@Slf4j
//@Component
//public class PbHeTianHelper {
//
//    @Autowired
//    private PbConfig pbConfig;
//
//    @Autowired
//    private PanbaoClient client ;
//
//    /**
//     * 订单请求接口
//     */
//    public RiskResultResponse creditApply(ApplyWithCreditRequest applyWithCreditRequest) {
//        ApplyWithCreditRequest request = new ApplyWithCreditRequest();
//        request.setMerchantId(pbConfig.getMerchantId());
//        request.setProductId(pbConfig.getProductId());
//        request.setLoanNo(applyWithCreditRequest.getLoanNo());
//        //模型好从开放平台获取，这里只是例子
//        request.setVersion(pbConfig.getVersion());
//        request.setUserName(applyWithCreditRequest.getUserName());
//        request.setCardNum(applyWithCreditRequest.getCardNum());
//        request.setMobile(applyWithCreditRequest.getMobile());
//        request.setTimeStamp(applyWithCreditRequest.getTimeStamp());
////        request.setOther("可以不填这个字段");
//        request.setRiskData(applyWithCreditRequest.getRiskData());
//        log.info("订单请求接口请求数据:" + JSON.toJSONString(applyWithCreditRequest));
//        RiskResultResponse response = client.creditRequest(applyWithCreditRequest);
//        log.info("订单请求接口返回结果:" + JSON.toJSONString(response));
//        return response;
//    }
//
//    /**
//     * 订单查询接口
//     */
//    public  BaseResponse queryCreditResult( QueryCreditResultRequest request ) {
//        QueryCreditResultRequest request = new QueryCreditResultRequest();
//        request.setMerchantId(pbConfig.getMerchantId());
//        request.setProductId(pbConfig.getProductId());
//        request.setBizType("001");
//        request.setLoanNo(loanNo);
//        request.setOrderDate(orderDate);
//        request.setTimeStamp(timeStamp);
//        log.info("订单查询接口请求数据:" + JSON.toJSONString(request));
//        BaseResponse baseResponse = client.queryRequest(request);
//        log.info("订单查询接口返回结果:" + JSON.toJSONString(baseResponse));
//        return baseResponse;
//    }
//
//
//}
