package com.mod.loan.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.util.SignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kk
 */
public class CallBackJuHeUtil {
    private static Logger logger = LoggerFactory.getLogger(CallBackJuHeUtil.class);

    public static boolean callBack(String host, JSONObject object) {
        object.put("timeStamp", System.currentTimeMillis() / 1000);
        String sign = SignUtil.getSign(object);
        object.put("sign", sign);

        String url = host + "/apiext/orderCallback/orderInfo";
        String response = HttpUtils.restRequest(url, object.toJSONString(), "POST");
        JSONObject res = JSON.parseObject(response);
        if ("200".equals(res.getString("code"))) {
            boolean i = true;
        }
        System.out.println(response + " === " + res.getString("0"));
        return false;
    }

}
