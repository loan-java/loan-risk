package com.mod.loan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.api.QjldHeTianApi;
import com.mod.loan.config.pb.PbConfig;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.model.DTO.EngineResult;
import com.mod.loan.model.DTO.QjldHeTianBaseReqDTO;
import com.mod.loan.model.DTO.QjldHeTianResDTO;
import com.mod.loan.util.pbUtil.PanbaoClient;
import com.mod.loan.util.pbUtil.SlpPanbaoBuilder;
import com.mod.loan.util.pbUtil.dto.request.ApplyWithCreditRequest;
import com.mod.loan.util.pbUtil.dto.request.QueryCreditResultRequest;
import com.mod.loan.util.pbUtil.dto.response.BaseResponse;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

/**
 * 决策请求工具类
 *
 * @author lijing
 * @date 2017/12/26 0026.
 */
@Slf4j
@Component
public class PbHeTianHelper {

    @Autowired
    private PbConfig pbConfig;

    private static PanbaoClient client ;

    String priKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ07Mvbw3ORoniC0vJR0tN8wYj5d45Xm/6z1qCy3UNlouI6RrAeNLePoWXKwtB/rYw2nQCvmLKwc7LGIYBN3v4Ozo+Sm0XfXzJQLq/gpPJkqUBY496WNp4DFPtxlKtgzyn8yryE12tN66cGvokUs0As3KJ6DOjD5RpFvZtGFWsenAgMBAAECgYA6g7uV11H910lyFpFQFpbxsQn+9+Yo9Y0Vi18JNTfrkWON+3ywWsRHW0NewZBniKyWejYRoFnju+gCsiQ/WoN1IpaxxS6V6D6cMrOR68fUvK3L0XeNINK3z+LNlWOWYICQekU4M9Z3kshrA7IN5s2e03VI4uyghmrmkNg06rEpsQJBAO8Lne9wMdWk9kAjHqQkxfGEiAD0IFtQnl1Xfk5XQ01Ch07SU4GANFIu6QrRKrhMPgGPWLtvcMjgsRosktENawsCQQCoYhIRtKoBr/+FYYu1ZhQf2c3VOyS2ZyrteINj6yiJrDYqS2gR84OgDLfPkRTR+ijZnBT4qr5PTfSysp3lANdVAkEAwAWf7+g7T/h8i5bqlUao71LcAZAZseC35hlTb9DvUk+/65RO2jdHUEE4mKTfYT51jME+sQpbGjJMcc8efOn/cwJAepiM119WuEvQGujes4BS/q2x86JQJI4Q7z1jTIiWx0YfWcP1TbyjBRC9c98J7afZvjap/jMyxMLZepIZ6/Yk0QJBANMlZUeRpdWYnKXCY7h4FpEQVmY8wr/90ag9Y22uUsOR1rZIt6hv+AtfAiSy73A9s9kDormMjts7VkflG8LT/Vw=";

    /**
     * 初始化加载
     */
    @PostConstruct
    public void init() {
        log.info("加载决策地址ip:{}", pbConfig.toString());
        client = SlpPanbaoBuilder.build("2", priKey)
                .test();  //测试环境
//                .pro();  //生产环境
    }

    /**
     * 订单请求接口
     * @param map
     */
    public static RiskResultResponse creditApply(Map<String,String> map) {
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
        return response;
    }

    /**
     * 订单查询接口
     * @param map
     */
    public static BaseResponse queryCreditResult(Map<String,String> map) {
        QueryCreditResultRequest request = new QueryCreditResultRequest();
        request.setMerchantId("2");
        request.setProductId("4");
        request.setBizType("001");
        request.setLoanNo("SLPTEST201901281007");
        request.setOrderDate("20190129");
        request.setTimeStamp("2019-01-28 14:48:01");
        BaseResponse baseResponse = client.queryRequest(request);
        System.out.println(JSON.toJSONString(baseResponse));
        return baseResponse;
    }


}
