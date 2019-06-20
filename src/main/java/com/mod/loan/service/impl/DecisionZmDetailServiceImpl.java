package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.config.pb.PbConfig;
import com.mod.loan.config.zm.ZmConfig;
import com.mod.loan.mapper.*;
import com.mod.loan.model.*;
import com.mod.loan.service.DecisionZmDetailService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.service.OrderUserService;
import com.mod.loan.util.DateUtil;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.pbUtil.PanbaoClient;
import com.mod.loan.util.pbUtil.dto.request.ApplyWithCreditRequest;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
import com.mod.loan.util.rongze.RongZeRequestUtil;
import com.mod.loan.util.zmUtil.Contact;
import com.mod.loan.util.zmUtil.EmergencyContact;
import com.mod.loan.util.zmUtil.ZhimiRiskRequest;
import com.mod.loan.util.zmUtil.ZhimiRiskUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
public class DecisionZmDetailServiceImpl extends BaseServiceImpl<DecisionZmDetail, Long> implements DecisionZmDetailService {

    @Autowired
    private DecisionZmDetailMapper zmDetailMapper;
    @Autowired
    private ZmConfig zmConfig;
    @Autowired
    private UserBankMapper userBankMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private OrderUserService orderUserService;

    @Autowired
    private MerchantService merchantService;

    @Override
    public DecisionZmDetail creditApply(User user, String orderNo) throws Exception {
        OrderUser orderUser = orderUserService.selectByOrderNo(orderNo);
        DecisionZmDetail zmDetail = null;
        Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
        try {
            UserInfo userInfo = userInfoMapper.selectByPrimaryKey(user.getId());
            //开始拼接数据
            String times = new DateTime().toString(TimeUtils.dateformat5);
            String timstramp = new DateTime().toString(TimeUtils.dateformat1);
//            String serials_no = String.format("%s%s%s", "p", times, user.getId());
            String model_name = zmConfig.getModelName();
            String product = orderNo;
            String channel = merchant.getMerchantName();
            String applyTime = timstramp;
            String mobile = user.getUserPhone();
            String name = user.getUserName();
            String idcard = user.getUserCertNo();
            String user_address = user.getAddress();
//            String companyName = userInfo.getWorkCompany();
//            String companyAddress = userInfo.getWorkAddress();
            Map<String, String> carrier_data = new HashMap<String, String>();
            carrier_data.put("mx_report", JSON.toJSONString(jxlAccessReport(orderNo), SerializerFeature.WriteMapNullValue));
            carrier_data.put("mx_raw", JSON.toJSONString(jxlOriginalData(orderNo), SerializerFeature.WriteMapNullValue));

            ZhimiRiskRequest request = new ZhimiRiskRequest();
            request.setModel_name(model_name);
            request.setProduct(product);
            request.setChannel(channel);
            request.setApply_time(applyTime);
            request.setMobile(mobile);
            request.setName(name);
            request.setIdcard(idcard);
            request.setUser_address(user_address);
            request.setCarrier_data(carrier_data);
            request.setE_contacts(emergencyContacts(userInfo));
            request.setContact(contactList(user));
            String requestStr = JSON.toJSONString(request, SerializerFeature.WriteMapNullValue);
            String url = zmConfig.getZmUrl();
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);
            post.setEntity(new ByteArrayEntity(ZhimiRiskUtil.gzip(requestStr)));
            log.info("=========指谜风控请求信息,orderNo=" + orderNo + "===========" + JSONObject.toJSONString(post));
            HttpResponse response = client.execute(post);
            String responseStr = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            log.info("=========指谜风控请求返回结果,orderNo=" + orderNo + "===========" + responseStr);
           if(!StringUtils.isEmpty(responseStr)){
               JSONObject jsonObject = JSONObject.parseObject(responseStr);
               if(0 == jsonObject.getInteger("return_code")){
                   zmDetail = new DecisionZmDetail();
                   zmDetail.setReturnCode(String.valueOf(jsonObject.getInteger("return_code")));
                   zmDetail.setReturnInfo(String.valueOf(jsonObject.getString("return_info")));
                   zmDetail.setRequestId(String.valueOf(jsonObject.get("request_id")));
                   zmDetail.setScore(String.valueOf(jsonObject.getInteger("score")));
                   zmDetail.setHistoryApply(jsonObject.getString("history_apply"));
                   zmDetail.setOrderNo(orderNo);
                   zmDetail.setCreatetime(new Date());
                   zmDetail.setUpdatetime(new Date());
                   zmDetailMapper.insert(zmDetail);
               }else{
                   //拒绝状态直接返回审批失败
                   zmDetail = new DecisionZmDetail();
                   zmDetail.setReturnCode("-1");
                   zmDetail.setReturnInfo("fail");
                   zmDetail.setScore("0.0");
                   zmDetail.setOrderNo(orderNo);
                   zmDetail.setCreatetime(new Date());
                   zmDetail.setUpdatetime(new Date());
                   zmDetailMapper.insert(zmDetail);
                   log.info("=========拒绝状态直接返回审批失败,orderNo=" + orderNo + "===========");
               }
           }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("订单请求出错", e);
        }
        return zmDetail;
    }

    //聚信立
    public JSONObject jxlOriginalData(String orderNo) {
        JSONObject report = null;
        try {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("order_no", orderNo);
            jsonObject1.put("type", "1");
            for (int times = 0; times < 10 && report == null; times++) {
                String result = RongZeRequestUtil.doPost(Constant.rongZeQueryUrl, "api.charge.data", jsonObject1.toJSONString());
//                log.warn("原始运营商报告数据的融泽返回结果："+result);
                //判断运营商数据
                JSONObject jsonObject = JSONObject.parseObject(result);
                if (jsonObject.containsKey("data")) {
                    String dataStr = jsonObject.getString("data");
                    JSONObject all = JSONObject.parseObject(dataStr);
                    if (all.containsKey("data")) {
                        JSONObject data = all.getJSONObject("data");
                        if (data.containsKey("report")) {
                            report = data.getJSONObject("report");
                        }
                    }
                }
                log.info(orderNo + "原始运营商报告数据当前获取运营报告循环次数:{}", times);
            }
            if (report != null) {
                if (report.containsKey("members")) {
                    JSONObject members = report.getJSONObject("members");
                    if (members.containsKey("transactions")) {
                        JSONArray transactions = members.getJSONArray("transactions");
                        if (transactions.size() > 0) {
                            JSONObject transactionsJson = (JSONObject) transactions.get(0);
                            if (transactionsJson.containsKey("smses")) {
                                JSONArray smses = transactionsJson.getJSONArray("smses");
                                int n = smses.size();
                                for (int i = 0; i < n; i++) {
                                    JSONObject smsesJson = (JSONObject) smses.get(i);
                                    if (!smsesJson.containsKey("other_cell_phone") || smsesJson.get("other_cell_phone") == null) {
                                        return null;
                                    }
                                }
                            }
                        }
                    }
                }
            }
//            log.warn("原始运营商报告数据:{},{}", orderNo, report == null ? null : report.toJSONString());
        } catch (Exception e) {
            log.error(orderNo + "获取原始运营商报告数据出错", e);
        }
        return report;
    }

    public JSONObject jxlAccessReport(String orderNo) {
        JSONObject report = null;
        try {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("order_no", orderNo);
            jsonObject1.put("type", "2");
            for (int times = 0; times < 10 && report == null; times++) {
                String result = RongZeRequestUtil.doPost(Constant.rongZeQueryUrl, "api.charge.data", jsonObject1.toJSONString());
//                log.warn("聚信立运营商报告数据的融泽返回结果："+result);
                //判断运营商数据
                JSONObject jsonObject = JSONObject.parseObject(result);
                if (jsonObject.containsKey("data")) {
                    String dataStr = jsonObject.getString("data");
                    JSONObject all = JSONObject.parseObject(dataStr);
                    if (all.containsKey("data")) {
                        JSONObject data = all.getJSONObject("data");
                        if (data.containsKey("report")) {
                            report = data.getJSONObject("report");
                        }
                    }
                }
                log.info(orderNo + "聚信立运营商报告数据当前获取运营报告循环次数:{}", times);
            }
//            log.warn("聚信立运营商报告数据:{}, {}", orderNo, report == null ? null : report.toJSONString());
        } catch (Exception e) {
            log.error(orderNo + "获取聚信立运营商报告数据出错", e);
        }
        return report;
    }

    //紧急联系人(至少2个)(必填)
//    {
//        "relation": "父亲",
//            "name": "秦世伦",
//            "phone": "18677392163"
//    },
//    {
//        "relation": "朋友",
//            "name": "曹武飞",
//            "phone": "13878384615"
//    },
    public List<EmergencyContact> emergencyContacts(UserInfo userInfo) {
        List<EmergencyContact> e_contacts = new ArrayList<EmergencyContact>();
        e_contacts.add(new EmergencyContact(userInfo.getDirectContactName(), userInfo.getDirectContactPhone()));
        e_contacts.add(new EmergencyContact(userInfo.getOthersContactName(), userInfo.getOthersContactPhone()));
        return e_contacts;
    }

    /*
    contact: 手机上爬取的通讯录(非必填，有的话建议填写)
     */
    public  List<Contact> contactList(User user) {
        List<Contact> contact = new ArrayList<Contact>();
        //  contact.add(new Contact("contact_name", "contact_phone", "update_time"));
        return contact;
    }

    /**
     * score: 第三方评分(非必填，有的话建议填写)
     * name: 评分名称
     * score: 评分值
     */
    public JSONArray score(User user) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonArray.add(jsonObject1);
        return jsonArray;
    }

    /*
    sms_list: 手机上爬取的短信(非必填，有的话建议填写)
	name: 姓名
	phone: 电话
	type:类型(发送/接收)
	time:时间
     */
    public JSONArray smsList(User user) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonArray.add(jsonObject1);
        return jsonArray;
    }

    /*
    callrecord_list: 手机上爬取的通话记录(非必填，有的话建议填写)
	name: 姓名
	phone: 电话
	type:类型(呼入/呼出/未接/拒接)
	start_time:电话开始时间
	end_time:电话结束时间(未接或未接通则空)
	use_time:通话时长(未接通为0，未接和拒接显示响铃次数(如有，否则也为0))
     */
    public JSONArray callrecordList(User user) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonArray.add(jsonObject1);
        return jsonArray;
    }


    @Override
    public DecisionZmDetail selectByOrderNo(String orderNo) throws Exception {
        return zmDetailMapper.selectByOrderNo(orderNo);
    }


}
