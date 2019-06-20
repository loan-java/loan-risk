package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.config.pb.PbConfig;
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
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DecisionZmDetailServiceImpl extends BaseServiceImpl<DecisionZmDetail, Long> implements DecisionZmDetailService {

    @Autowired
    private DecisionZmDetailMapper zmDetailMapper;
    @Autowired
    private PbConfig pbConfig;
    @Autowired
    private PanbaoClient client;
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
            ///设置缓存
//            redisMapper.set(serials_no, user.getId()+"&" + times);
            //开始请求
            ApplyWithCreditRequest request = new ApplyWithCreditRequest();
            request.setMerchantId(pbConfig.getMerchantId());
            request.setProductId(pbConfig.getProductId());
            request.setLoanNo(orderNo);//todo 这里要修改为 orderNo
            //模型好从开放平台获取，这里只是例子
            request.setVersion(pbConfig.getVersion());
            request.setUserName(user.getUserName());
            request.setCardNum(user.getUserCertNo());
            request.setMobile(user.getUserPhone());
            request.setTimeStamp(timstramp);
            request.setOther(merchant.getMerchantName());
            JSONObject riskData = new JSONObject();
            riskData.put("name", user.getUserName());
            riskData.put("mobile", user.getUserPhone());
            riskData.put("idNum", user.getUserCertNo());
            riskData.put("live_addr", userInfo.getLiveAddress() != null ? userInfo.getLiveAddress() : user.getAddress());
            riskData.put("cardSetAddr", user.getAddress());
            riskData.put("gender", StringUtil.execute(user.getUserCertNo()));
            riskData.put("apply_channel", "rongze");
            riskData.put("nation", user.getNation());

            if (userInfo != null) {
                riskData.put("emergency_contacts", emergencyContacts(userInfo));
                riskData.put("company_addr", userInfo.getWorkAddress());
                //riskData.put("company_phone", null);
                riskData.put("company_name", userInfo.getWorkCompany());
            }
            // riskData.put("contact_list", contactList(user));  //需要转化
            //riskData.put("score", score(user));
            //riskData.put("sms_list", smsList(user));
            //riskData.put("callrecord_list", callrecordList(user));
            // 查询用户是否是复贷用户
            List<Order> orderList = orderMapper.getDoubleLoanByUid(user.getId());
            if (orderList.size() < 1) {
                riskData.put("renew_loan", "0");
            } else {
                riskData.put("renew_loan", "1");
            }
            riskData.put("installment", "1");//期次
            String approvalAmount = "1500"; //审批金额
            String approvalTerm = "6"; //审批期限
            String termUnit = "1"; //期限单位，1 - 天
            riskData.put("apply_amount", approvalAmount);
            riskData.put("apply_time", orderUser.getCreateTime());
            UserBank userBank = userBankMapper.selectUserCurrentBankCard(user.getId());
            if (userBank != null) {
                riskData.put("debit_card", userBank.getCardNo());
                riskData.put("debit_card_reserved_mobile", userBank.getCardPhone());
            }
            //行为信息(复贷用户填写)
            if (riskData.get("renew_loan").toString().equals("1")) {
                JSONObject behavior = new JSONObject();
                behavior.put("card_add_list", cardAddList(user, userBank));
                behavior.put("his_order_list", hisOrderList(user));
                riskData.put("behavior", behavior);
            }
            //如为聚信立
            riskData.put("jxlAccessReport", jxlAccessReport(orderNo));
            riskData.put("jxlOriginalData", jxlOriginalData(orderNo));
            log.info("=========订单："+orderNo+"聚信立信息,聚信立运营商报告是否为空：" + (riskData.get("jxlAccessReport") == null) + "===========");
            log.info("=========订单："+orderNo+"聚信立信息,原始运营商报告是否为空：" + ( riskData.get("jxlOriginalData") == null) + "===========");
            //判断是否存在
            if (riskData.get("jxlAccessReport") == null || riskData.get("jxlOriginalData") == null) {
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
                return zmDetail;
            }
            request.setRiskData(riskData);
            RiskResultResponse response = client.creditRequest(request);
            log.info("订单请求接口返回结果:" + JSON.toJSONString(response));
            //开始封装数据
            if (response != null) {
                zmDetail = new DecisionZmDetail();
                zmDetail.setOrderNo(orderNo);
                String result = response.getResult();

                if (response.getRspCode().equals("000000")) {
                    if (PbResultEnum.APPROVE.getCode().equals(result)) {

                    }
                }
                zmDetailMapper.insert(zmDetail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("订单请求出错", e);
        }
        return zmDetail;
    }


    //银行卡添加记录
    //    \"card_add_list\":[{" +
//           \" reserved_mobile \": \"13299332222\"," +
//               \" bank_short_name\": \"ICBC\"," +
//              \" bank_card_type \": \"debitcard\"," +
//                 \"add_time \": \"2010-01-01 12:12:12\"" +
//             }]," +
    public JSONArray cardAddList(User user, UserBank userBank) {
        JSONArray jsonArray = new JSONArray();
        if (userBank != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("reserved_mobile", userBank.getCardPhone());
            jsonObject.put("bank_short_name", userBank.getCardCode());
            jsonObject.put("bank_card_type", "debitcard");
            jsonObject.put("add_time", DateUtil.dateToStrLong(userBank.getCreateTime()));
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

    //历史借款信息
//    \"his_order_list\":[{" +
//           \"order_id\": \"1234\"," +
//           \"order_time\": \"2010-01-01 12:12:12\"," +
//           \"apply_amount\": \"1000.00\"," +
//            \"loan_time\": \"2010-01-01 12:12:23\"," +
//             \"plan_repay_time\": \"2010-02-01 12:12:12\"," +
//           \"actual_repay_time\": \"2010-03-01 12:12:12\"," +
//           \"loan_amount\": \"1000.00\"," +
//           \"plan_repay_amount\": \"1100.00\"," +
//               \"actual_repay_amount\": \"1100.00\"" +
//         }]" +
    public JSONArray hisOrderList(User user) {
        JSONArray jsonArray = new JSONArray();
        List<Order> orders = orderMapper.getOrderByUid(user.getId());
        if (orders != null && orders.size() > 0) {
            for (Order order : orders) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("order_id", order.getOrderNo());
                jsonObject.put("order_time", DateUtil.dateToStrLong(order.getCreateTime()));
                jsonObject.put("apply_amount", order.getBorrowMoney());
                jsonObject.put("loan_time", order.getArriveTime() != null ? DateUtil.dateToStrLong(order.getArriveTime()) : null);
                jsonObject.put("plan_repay_time", order.getRepayTime() != null ? DateUtil.dateToStrLong(order.getRepayTime()) : null);
                jsonObject.put("actual_repay_time", order.getRealRepayTime() != null ? DateUtil.dateToStrLong(order.getRealRepayTime()) : null);
                jsonObject.put("loan_amount", order.getActualMoney() != null ? order.getActualMoney() : 0);
                jsonObject.put("plan_repay_amount", order.getShouldRepay() != null ? order.getShouldRepay() : 0);
                jsonObject.put("actual_repay_amount", order.getHadRepay() != null ? order.getHadRepay() : 0);
                jsonArray.add(jsonObject);
            }
        }
        return jsonArray;
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
    public JSONArray emergencyContacts(UserInfo userInfo) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("relation", userInfo.getDirectContact());
        jsonObject1.put("name", userInfo.getDirectContactName());
        jsonObject1.put("phone", userInfo.getDirectContactPhone());
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("relation", userInfo.getOthersContact());
        jsonObject2.put("name", userInfo.getOthersContactName());
        jsonObject2.put("phone", userInfo.getOthersContactPhone());
        jsonArray.add(jsonObject1);
        jsonArray.add(jsonObject2);
        return jsonArray;
    }

    /*
    contact_list: 手机上爬取的通讯录(非必填，有的话建议填写)
	name: 姓名
	phone: 电话
     */
    public JSONArray contactList(User user) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonArray.add(jsonObject1);
        return jsonArray;
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
