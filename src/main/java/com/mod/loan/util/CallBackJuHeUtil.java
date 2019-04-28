package com.mod.loan.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kk
 */
@Slf4j
public class CallBackJuHeUtil {

    public static boolean callBack(String host, JSONObject object) {
        object.put("timeStamp", System.currentTimeMillis() / 1000);
        log.info("回调参数", String.valueOf(object));
        String sign = SignUtil.getSign(object);
        object.put("sign", sign);
        boolean successFlag = false;
        String url = host + "/apiext/orderCallback/orderInfo";
        String response = HttpUtils.restRequest(url, object.toJSONString(), "POST");
        JSONObject res = JSON.parseObject(response);
        if ("200".equals(res.getString("code"))) {
            successFlag = true;
        }
        log.info(response + " === " + res.getString("0"));
        return successFlag;
    }

}
