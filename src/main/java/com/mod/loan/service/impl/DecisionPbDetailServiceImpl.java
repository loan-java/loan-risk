package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mod.loan.common.enums.OrderSourceEnum;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.mapper.BaseServiceImpl;
import com.mod.loan.config.Constant;
import com.mod.loan.config.pb.PbConfig;
import com.mod.loan.mapper.*;
import com.mod.loan.model.*;
import com.mod.loan.service.DecisionPbDetailService;
import com.mod.loan.service.MerchantService;
import com.mod.loan.util.DateUtil;
import com.mod.loan.util.StringUtil;
import com.mod.loan.util.TimeUtils;
import com.mod.loan.util.bengbeng.BengBengRequestUtil;
import com.mod.loan.util.pbUtil.PanbaoClient;
import com.mod.loan.util.pbUtil.dto.request.ApplyWithCreditRequest;
import com.mod.loan.util.pbUtil.dto.request.QueryCreditResultRequest;
import com.mod.loan.util.pbUtil.dto.response.QueryRiskResultResponse;
import com.mod.loan.util.pbUtil.dto.response.RiskResultResponse;
import com.mod.loan.util.rongze.RongZeRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class DecisionPbDetailServiceImpl extends BaseServiceImpl<DecisionPbDetail, Long> implements DecisionPbDetailService {

    @Autowired
    private DecisionPbDetailMapper pbDetailMapper;
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


    @Autowired
    private MerchantService merchantService;

    @Resource
    private MerchantRateMapper merchantRateMapper;

    //2.2接口调用
    @Override
    public DecisionPbDetail creditApply(User user, Order order) throws Exception {
        DecisionPbDetail decisionPbDetail = null;
        try {
            Merchant merchant = merchantService.findMerchantByAlias(user.getMerchant());
            if (merchant == null) {
                throw new Exception("十路盘风控查询:商户不存" + user.getMerchant());
            }
            //是否存在关联的借贷信息
            if (order.getProductId() == null) {
                throw new Exception("十路盘风控查询:商户不存在Order关联的借贷信息" + order.toString());
            }
            MerchantRate merchantRate = merchantRateMapper.selectByPrimaryKey(order.getProductId());
            if (merchantRate == null) {
                throw new Exception("十路盘风控查询:商户不存在默认的借贷信息");
            }
            BigDecimal approvalAmount1 = merchantRate.getProductMoney(); //审批金额
            if (approvalAmount1 == null) {
                throw new Exception("十路盘风控查询:商户不存在默认借贷金额" + merchantRate.toString());
            }
            Integer approvalTerm1 = merchantRate.getProductDay(); //审批期限
            if (approvalTerm1 == null) {
                throw new Exception("十路盘风控查询:商户不存在默认借贷期限" + merchantRate.toString());
            }

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
            request.setLoanNo(order.getOrderNo());
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
            riskData.put("nation", user.getNation() != null ? user.getNation() : "汉");

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
            String approvalAmount = String.valueOf(approvalAmount1.intValue()); //审批金额
            String approvalTerm = String.valueOf(approvalTerm1.intValue()); //审批期限
            String termUnit = "1"; //期限单位，1 - 天
            riskData.put("apply_amount", approvalAmount);
            riskData.put("apply_time", order.getCreateTime());
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
            riskData.put("jxlAccessReport", jxlAccessReport(order.getOrderNo(), order.getSource()));
            riskData.put("jxlOriginalData", jxlOriginalData(order.getOrderNo(), order.getSource()));
            log.info("订单：" + order.getOrderNo() + "聚信立信息,聚信立运营商报告是否为空：" + (riskData.get("jxlAccessReport") == null));
            log.info("订单：" + order.getOrderNo() + "聚信立信息,原始运营商报告是否为空：" + (riskData.get("jxlOriginalData") == null));
            //判断是否存在
            if (riskData.get("jxlAccessReport") == null || riskData.get("jxlOriginalData") == null) {
                //拒绝状态直接返回审批失败
                decisionPbDetail = new DecisionPbDetail();
                decisionPbDetail.setOrderId(order.getId());
                decisionPbDetail.setResult(PbResultEnum.DENY.getCode());
                decisionPbDetail.setScore("0.0");
                decisionPbDetail.setDesc("拒绝");
                decisionPbDetail.setMsg("成功");
                decisionPbDetail.setCode("000000");
                decisionPbDetail.setOrderNo(order.getOrderNo());
                decisionPbDetail.setLoanMoney(0L);
                decisionPbDetail.setCreatetime(new Date());
                decisionPbDetail.setUpdatetime(new Date());
                pbDetailMapper.insert(decisionPbDetail);
                log.info("拒绝状态直接返回审批失败,orderNo:{}", order.getOrderNo());
                return decisionPbDetail;
            }
            request.setRiskData(riskData);
            RiskResultResponse response = client.creditRequest(request);
            log.info("订单请求接口返回结果:{}", JSON.toJSONString(response));
            //开始封装数据
            if (response != null) {
                decisionPbDetail = new DecisionPbDetail();
                decisionPbDetail.setOrderId(order.getId());
                decisionPbDetail.setOrderNo(order.getOrderNo());
                String result = response.getResult();
                decisionPbDetail.setResult(result);
                decisionPbDetail.setDesc(response.getDesc());
                decisionPbDetail.setCode(response.getRspCode());
                decisionPbDetail.setMsg(response.getRspMsg());
                decisionPbDetail.setScore(response.getScore());
                decisionPbDetail.setCreatetime(new Date());
                decisionPbDetail.setUpdatetime(new Date());
                if (response.getRspCode().equals("000000")) {
                    if (PbResultEnum.APPROVE.getCode().equals(result)) {
                        decisionPbDetail.setLoanMoney(response.getLoanAmount() != null ? Long.valueOf(response.getLoanAmount()) : null);
                        decisionPbDetail.setLoanNumber(response.getLoanNumber());
                        decisionPbDetail.setLoanRate(response.getLoanRate());
                        decisionPbDetail.setLoanUnit(response.getLoanUnit());
                        // decisionPbDetail.setLoanNo(response.getSlpOrderNo());
                    }
                }
                pbDetailMapper.insert(decisionPbDetail);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            log.error("订单请求出错", e);
        }
        return decisionPbDetail;
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

    //原始
    public JSONObject jxlOriginalData(String orderNo, Integer soruce) {
        JSONObject report = null;
        try {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("order_no", orderNo);
            jsonObject1.put("type", "1");
            for (int times = 0; times < 10 && report == null; times++) {
                String result = null;
                if (OrderSourceEnum.isRongZe(soruce)) {
                    result = RongZeRequestUtil.doPost(Constant.rongZeQueryUrl, "api.charge.data", jsonObject1.toJSONString());
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
                } else if (OrderSourceEnum.isBengBeng(soruce)) {
                    result = BengBengRequestUtil.doPost(Constant.bengBengQueryUrl, "api.charge.data", jsonObject1.toJSONString());
                    //判断运营商数据
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    if (jsonObject.containsKey("data")) {
                        String dataStr = jsonObject.getString("data");
                        JSONObject all = JSONObject.parseObject(dataStr);
                        if (all.containsKey("raw_data")) {
                            report = all.getJSONObject("raw_data");
                        }
                    }
                } else {
                    return null;
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
                            if (!transactionsJson.containsKey("transactions") || transactionsJson.get("transactions") == null) {
                                return null;
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

    //聚信立
    public JSONObject jxlAccessReport(String orderNo, Integer soruce) {
        JSONObject report = null;
        try {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("order_no", orderNo);
            jsonObject1.put("type", "2");
            for (int times = 0; times < 10 && report == null; times++) {
                String result = null;
                if (OrderSourceEnum.isRongZe(soruce)) {
                    result = RongZeRequestUtil.doPost(Constant.rongZeQueryUrl, "api.charge.data", jsonObject1.toJSONString());
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
                } else if (OrderSourceEnum.isBengBeng(soruce)) {
                    result = BengBengRequestUtil.doPost(Constant.bengBengQueryUrl, "api.charge.data", jsonObject1.toJSONString());
                    //判断运营商数据
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    if (jsonObject.containsKey("data")) {
                        String dataStr = jsonObject.getString("data");
                        JSONObject all = JSONObject.parseObject(dataStr);
                        if (all.containsKey("report_data")) {
                            report = all.getJSONObject("report_data");
                        }
                    }
                } else {
                    return null;
                }

                log.info(orderNo + "聚信立运营商报告数据当前获取运营报告循环次数:{}", times);
            }
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


    //2.3接口调用
    @Override
    public DecisionPbDetail queryCreditResult(DecisionPbDetail detail) throws Exception {
        try {
            String loanNo = detail.getOrderNo();
            String timeStamp = DateUtil.dateToStrLong(detail.getCreatetime());
            String orderDate = DateUtil.dateToYYYYMMDD(detail.getCreatetime());
            QueryCreditResultRequest request = new QueryCreditResultRequest();
            request.setMerchantId(pbConfig.getMerchantId());
            request.setProductId(pbConfig.getProductId());
            request.setBizType("001");
            request.setLoanNo(loanNo);
            request.setOrderDate(orderDate);
            request.setTimeStamp(timeStamp);
            log.info("订单查询接口请求数据:{}", JSON.toJSONString(request));
            QueryRiskResultResponse baseResponse = client.queryRequest(request);
            log.info("订单查询接口返回结果:{}", JSON.toJSONString(baseResponse));
            //开始封装数据
            if (baseResponse != null) {
                detail.setCode(baseResponse.getRspCode());
                detail.setMsg(baseResponse.getRspMsg());
                detail.setUpdatetime(new Date());
                if (baseResponse.getRspCode().equals("000000") && "S".equals(baseResponse.getOrderStatus())) {
                    //直接更新风控信息
                    detail.setResult(baseResponse.getResult());
                    detail.setDesc(baseResponse.getDesc());
                    detail.setLoanMoney(baseResponse.getLoanAmount() != null ? Long.valueOf(baseResponse.getLoanAmount()) : null);
                    detail.setScore(baseResponse.getScore());
                    detail.setLoanNumber(baseResponse.getLoanNumber());
                    detail.setLoanRate(baseResponse.getLoanRate());
                    detail.setLoanUnit(baseResponse.getLoanUnit());
                    detail.setLoanNo(baseResponse.getSlpOrderNo());
                } else if (baseResponse.getRspCode().equals("000000") && ("P".equals(baseResponse.getOrderStatus()) || "I".equals(baseResponse.getOrderStatus()))) {
                    detail.setResult(PbResultEnum.HANDLING.getCode());
                    detail.setDesc(PbResultEnum.HANDLING.getText());
                } else {
                    detail.setResult(PbResultEnum.DENY.getCode());
                    detail.setDesc("拒绝");
                }
                pbDetailMapper.updateByPrimaryKeySelective(detail);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("订单查询接口出错", e);
        }
        return detail;
    }

    @Override
    public DecisionPbDetail selectByOrderNo(String orderNo) {
        return pbDetailMapper.selectByOrderNo(orderNo);
    }


}
