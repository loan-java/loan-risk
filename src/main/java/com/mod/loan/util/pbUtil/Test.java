package com.mod.loan.util.pbUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.pbUtil.dto.request.ApplyWithCreditRequest;
import com.mod.loan.util.pbUtil.dto.request.QueryCreditResultRequest;
import com.mod.loan.util.pbUtil.dto.response.BaseResponse;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;

import java.util.ArrayList;

public class Test {

    public static void main(String[] args) {
        //商户方私钥
        String priKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ07Mvbw3ORoniC0vJR0tN8wYj5d45Xm/6z1qCy3UNlouI6RrAeNLePoWXKwtB/rYw2nQCvmLKwc7LGIYBN3v4Ozo+Sm0XfXzJQLq/gpPJkqUBY496WNp4DFPtxlKtgzyn8yryE12tN66cGvokUs0As3KJ6DOjD5RpFvZtGFWsenAgMBAAECgYA6g7uV11H910lyFpFQFpbxsQn+9+Yo9Y0Vi18JNTfrkWON+3ywWsRHW0NewZBniKyWejYRoFnju+gCsiQ/WoN1IpaxxS6V6D6cMrOR68fUvK3L0XeNINK3z+LNlWOWYICQekU4M9Z3kshrA7IN5s2e03VI4uyghmrmkNg06rEpsQJBAO8Lne9wMdWk9kAjHqQkxfGEiAD0IFtQnl1Xfk5XQ01Ch07SU4GANFIu6QrRKrhMPgGPWLtvcMjgsRosktENawsCQQCoYhIRtKoBr/+FYYu1ZhQf2c3VOyS2ZyrteINj6yiJrDYqS2gR84OgDLfPkRTR+ijZnBT4qr5PTfSysp3lANdVAkEAwAWf7+g7T/h8i5bqlUao71LcAZAZseC35hlTb9DvUk+/65RO2jdHUEE4mKTfYT51jME+sQpbGjJMcc8efOn/cwJAepiM119WuEvQGujes4BS/q2x86JQJI4Q7z1jTIiWx0YfWcP1TbyjBRC9c98J7afZvjap/jMyxMLZepIZ6/Yk0QJBANMlZUeRpdWYnKXCY7h4FpEQVmY8wr/90ag9Y22uUsOR1rZIt6hv+AtfAiSy73A9s9kDormMjts7VkflG8LT/Vw=";
        PanbaoClient client = new PanbaoClient();
        //2.2授信
        testCreditApply(client);
        //2.3查询
//        testQueryCreditResult(client);
        //2.6还款计划（批量）
//        testPushPlan(client);
        //2.7还款记录（批量）
//        testPushRecord(client);
        //2.8催收上传
//        testPushCollectionRecord(client);
    }

    public static void testCreditApply(PanbaoClient client) {
        ApplyWithCreditRequest request = new ApplyWithCreditRequest();
        request.setMerchantId("2");
        request.setProductId("4");
        request.setLoanNo("SLPTEST201901281001");
        //模型好从开放平台获取，这里只是例子
        request.setVersion("CESHI1.0");
        request.setUserName("测试");
        request.setCardNum("240321197609271711");
        request.setMobile("13855120928");
        request.setTimeStamp("2019-01-28 10:09:03");
        request.setOther("可以不填这个字段");
        String str="{" +
                "\"name\": \"秦德文\"," +
                "\"mobile\": \"18887749973\"," +
                "\"idNum\": \"450331199511070613\"," +
                "\"cardSetAddr\": \"广西荔浦县新坪镇凤岗村新村屯173号\"," +
                "\"gender\": \"男\"," +
                "\"renew_loan\": 0," +
                "\"installment\": 0," +
                "\"behavior\": {" +
                "            \"card_add_list\":[{" +
                "                       \" reserved_mobile \": \"13299332222\"," +
                "                       \" bank_short_name\": \"ICBC\"," +
                "                       \" bank_card_type \": \"debitcard\"," +
                "                       \"add_time \": \"2010-01-01 12:12:12\"" +
                "                   }]," +
                "            \"his_order_list\":[{" +
                "                       \"order_id\": \"1234\"," +
                "                       \"order_time\": \"2010-01-01 12:12:12\"," +
                "                       \"apply_amount\": \"1000.00\"," +
                "                       \"loan_time\": \"2010-01-01 12:12:23\"," +
                "                       \"plan_repay_time\": \"2010-02-01 12:12:12\"," +
                "                       \"actual_repay_time\": \"2010-03-01 12:12:12\"," +
                "                       \"loan_amount\": \"1000.00\"," +
                "                       \"plan_repay_amount\": \"1100.00\"," +
                "                       \"actual_repay_amount\": \"1100.00\"" +
                "                   }]" +
                "}," +
                "\"jxlAccessReport\": {}," +
                "\"jxlOriginalData\": {}" +
                "}";
        request.setRiskData(JSONObject.parseObject(str));
        RiskResultResponse response = client.creditRequest(request);
        System.out.println("返回报文打印:"+ JSON.toJSONString(response));
    }

    public static void testQueryCreditResult(PanbaoClient client) {
        QueryCreditResultRequest request = new QueryCreditResultRequest();
        request.setMerchantId("2");
        request.setProductId("4");
        request.setBizType("001");
        request.setLoanNo("SLPTEST201901281007");
        request.setOrderDate("20190129");
        request.setTimeStamp("2019-01-28 14:48:01");
        BaseResponse baseResponse = client.queryRequest(request);
        System.out.println(JSON.toJSONString(baseResponse));
    }

//    public static void testPushPlan(PanbaoClient client) {
//        PushRepaymentPlanRequest request = new PushRepaymentPlanRequest();
//        request.setMerchantId("2");
//        request.setProductId("4");
//        request.setBizType("002");
//        List<RepaymentPlanBizContent> contentList = new ArrayList<>();
//        request.setBizContent(contentList);
//        RepaymentPlanBizContent content = new RepaymentPlanBizContent();
//        contentList.add(content);
//        content.setLoanNo("SLPTEST201901281007");
//        List<BatchPaymentPlanDto> dtoList = new ArrayList<>();
//        content.setRepayList(dtoList);
//        BatchPaymentPlanDto dto = new BatchPaymentPlanDto();
//        dtoList.add(dto);
//        dto.setLoanNumber(1);
//        dto.setRepaymentIndexs(1);
//        dto.setValueDate("2019-01-28 11:06:01");
//        dto.setRepayDate("2019-02-28 11:06:01");
//        dto.setCapital("3000");
//        dto.setInterest("24");
//        dto.setLoanRate("0.008");
//
//        BaseResponse response = client.pushRepayPlan(request);
//        System.out.println(JSON.toJSONString(response));
//    }
//
//    public static void testPushRecord(PanbaoClient client) {
//        PushRepaymentRecordRequest request = new PushRepaymentRecordRequest();
//        request.setMerchantId("2");
//        request.setProductId("4");
//        request.setBizType("003");
//        List<RepaymentRecordBizContent> contentList = new ArrayList<>();
//        request.setBizContent(contentList);
//        RepaymentRecordBizContent content = new RepaymentRecordBizContent();
//        contentList.add(content);
//        content.setLoanNo("SLPTEST201901281007");
//        content.setLoanNumber(1);
//        content.setLoanRepaymentIndexs(1);
//        content.setRepaymentStatus(0);
//        content.setRepayDate("2019-02-28 11:06:01");
//        content.setActualDate("2019-02-28 11:06:01");
//        content.setRepayAmount("3024");
//        content.setActualAmount("3024");
//        content.setCapital("3000");
//        content.setInterest("24");
//        content.setPenaltyAmount("0");
//        content.setSurchargeFee("0");
//        BaseResponse response = client.pushRepayRecord(request);
//        System.out.println(JSON.toJSONString(response));
//
//    }
//
//    public static void testPushCollectionRecord(PanbaoClient client) {
//        PushCollectionRecordRequest request = new PushCollectionRecordRequest();
//        request.setMerchantId("2");
//        request.setProductId("4");
//        request.setBizType("004");
//        List<CollectionRecordBizContent> contentList = new ArrayList<>();
//        request.setBizContent(contentList);
//        CollectionRecordBizContent content = new CollectionRecordBizContent();
//        contentList.add(content);
//        content.setLoanNo("SLPTEST201901281007");
//        content.setLoanRepaymentIndexs(1);
//        content.setCollectionDate("2018-01-30 13:23:11");
//        content.setResult("0");
//        List<CollectionDetailDto> dtoList = new ArrayList<>();
//        content.setCollDetail(dtoList);
//        CollectionDetailDto dto = new CollectionDetailDto();
//        dtoList.add(dto);
//        dto.setCurrCallPhoneNumber("13855120928");
//        dto.setCurrCollectionContent("请抓紧还款");
//        dto.setCurrCollectionDate("2019-01-30 12:12:21");
//        dto.setCurrCoolectionState("0");
//
//        BaseResponse response = client.pushRepayCollection(request);
//        System.out.println(JSON.toJSONString(response));
//    }

}