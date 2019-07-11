//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mod.loan.Application;
//import com.mod.loan.util.qjldUtil.DecisionHelper;
//import com.mod.loan.common.message.OrderPayMessage;
//import com.mod.loan.common.message.RiskAuditMessage;
//import com.mod.loan.config.Constant;
//import com.mod.loan.config.qjld.QjldConfig;
//import com.mod.loan.config.rabbitmq.RabbitConst;
//import com.mod.loan.model.DTO.*;
//import com.mod.loan.util.ConstantUtils;
//import com.mod.loan.util.RsaCodingUtil;
//import com.mod.loan.util.RsaReadUtil;
//import com.mod.loan.util.rongze.RongZeRequestUtil;
//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author lijing
// * @date 2018/1/12 0012.
// */
//@Slf4j
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = Application.class)
//public class DecisionTest {
//    public static final String CHAR_SET = "UTF-8";
//    @Autowired
//    private DecisionHelper decisionHelper;
//    @Autowired
//    private QjldConfig qjldConfig;
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Test
//    public void SyncTest() {
//        DecisionBaseReqDTO decisionReqDTO = new DecisionBaseReqDTO();
//        decisionReqDTO.setMember_id(qjldConfig.getQjldMemberId());
//        decisionReqDTO.setTerminal_id(qjldConfig.getQjldTerminalId());
//        decisionReqDTO.setRes_encrypt(Boolean.FALSE);
//        decisionReqDTO.setTrans_id("TEST" + System.currentTimeMillis());
//        String dataContent = getDataContent();
//        decisionReqDTO.setData_content(dataContent);
//        DecisionBaseResDTO decision = decisionHelper.syncDecision(decisionReqDTO);
//        DecisionResDetailDTO publicKey = decision.getData_content();
//        log.info("决策执行完成,响应信息:{}", publicKey);
//    }
//
//
////    @Test
////    public void NoSyncTest() {
////        DecisionBaseReqDTO decisionReqDTO = new DecisionBaseReqDTO();
////        decisionReqDTO.setMemberId(qjldConfig.getQjldMemberId());
////        decisionReqDTO.setTerminalId(qjldConfig.getQjldTerminalId());
////        decisionReqDTO.setEncrypt(Boolean.FALSE);
////        decisionReqDTO.setTransId("TEST" + System.currentTimeMillis());
////        String dataContent = getDataContent();
////        decisionReqDTO.setDataContent(dataContent);
////        DecisionBaseResDTO decision = decisionHelper.nosyncDecision(decisionReqDTO);
////        DecisionResDetailDTO publicKey = decision.getDataContent();
////        log.info("决策执行完成,响应信息:{}", publicKey);
////    }
//
//    @Test
//    public void QueryTest() {
//        DecisionReqDTO reqDTO = new DecisionReqDTO();
//        reqDTO.setMember_id(qjldConfig.getQjldMemberId());
//        reqDTO.setTrans_id("p201904222329294");
//        reqDTO.setNeed_details(ConstantUtils.Y_FLAG);
//        DecisionResDetailDTO decision = decisionHelper.queryDecision(reqDTO);
//        log.info("决策执行完成,响应信息:{}", decision);
//    }
//
//
//    /***
//     * 加密参数请求
//     *
//     * @return 加密数据
//     */
//    private String getDataContent() {
//
//        DecisionReqDTO reqDTO = new DecisionReqDTO();
//        //添加客户基本信息(姓名身份证必填)
//        BaseUserDTO userDTO = new BaseUserDTO();
//        userDTO.setId_name("杨平");
//        userDTO.setId_card("142729196303151633");
//        userDTO.setPhone("17096140427");
//        userDTO.setBank_card_no("6212260511007212158");
//        reqDTO.setBase_user(userDTO);
//        //添加商户信息(新颜分配的商户号)
//        reqDTO.setMember_id(qjldConfig.getQjldMemberId());
//        //添加事件编号(在配置页面配置事件后可以使用事件编号调用到对应事件)
//        reqDTO.setDecision_code(qjldConfig.getQjldType());
//        //是否返回明细
//        reqDTO.setNeed_details("Y");
//        //订单号(32位唯一字符串)
//        reqDTO.setTrans_id("TEST" + System.currentTimeMillis());
//        //添加自定义标签信息(该信息可以是map也可以是对象只要满足对应的json格式就可以)
//        //没有自定义标签的可以不传这个参数
//        reqDTO.setNotify_url("www.baidu.com");
//        Map<String, Object> customerParams = new HashMap<>();
//        customerParams.put("sex", "男");
//        reqDTO.setCustom_params(customerParams);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String jsonData = null;
//        try {
//            jsonData = objectMapper.writeValueAsString(reqDTO);
//            log.info("请求JSON数据:{}", jsonData);
//            jsonData = RsaCodingUtil.encryptByPrivateKey(Base64.encode(jsonData.getBytes(CHAR_SET)),
//                    RsaReadUtil.getPrivateKeyFromFile(qjldConfig.getQjldKeyPath(), qjldConfig.getQjldKeyPwd()));
//
//        } catch (Exception e) {
//            log.error("转换字符串异常:{}", e);
//        }
//
//        return jsonData;
//    }
//
//
//    @Test
//    public void putMqData() {
//        RiskAuditMessage message = new RiskAuditMessage();
//        message.setOrderId(10L);
//        message.setStatus(1);
//        message.setMerchant("jishidai");
//        message.setUid(25L);
//        rabbitTemplate.convertAndSend(RabbitConst.qjld_queue_risk_order_notify, message);
//    }
//
//
//    @Test
//    public void test11() {
//        JSONObject report = null;
//        try {
//            String tesr="{" +
//                    "\"members\": {" +
//                    "\"update_time\": \"2019-06-12 15:46:10\"," +
//                    "\"error_msg\": \"请求用户数据成功\"," +
//                    "\"request_args\": [{" +
//                    "\"token\": \"15127599eaea407c8339adc619e8a7b9\"" +
//                    "}, {" +
//                    "\"env\": \"www\"" +
//                    "}]," +
//                    "\"error_code\": 31200," +
//                    "\"transactions\": [{" +
//                    "\"smses\": [{" +
//                    "\"start_time\": \"2019-06-09 12:10:55\"," +
//                    "\"update_time\": \"2019-06-12 15:46:04\"," +
//                    "\"subtotal\": 0.0," +
//                    "\"other_cell_phone\": \"10010\"," +
//                    "\"cell_phone\": \"13046329502\"" +
//                    "}, {" +
//                    "\"start_time\": \"2019-06-08 21:01:48\"," +
//                    "\"update_time\": \"2019-06-12 15:46:04\"," +
//                    "\"subtotal\": 0.0," +
//                    "\"cell_phone\": \"13046329502\"" +
//                    "}]," +
//                    "\"basic\": {" +
//                    "\"update_time\": \"2019-06-12 15:46:04\"," +
//                    "\"idcard\": \"4311****2261\"," +
//                    "\"reg_time\": \"2015-10-11 00:00:00\"," +
//                    "\"real_name\": \"柏颖\"," +
//                    "\"cell_phone\": \"13046329502\"" +
//                    "}," +
//                    "\"version\": \"1\"," +
//                    "\"token\": \"15127599eaea407c8339adc619e8a7b9\"" +
//                    "}]," +
//                    "\"status\": \"success\"" +
//                    "}" +
//                    "}";
//            report=JSONObject.parseObject(tesr);
//            if(report != null) {
//                if(report.containsKey("members")) {
//                    JSONObject members = report.getJSONObject("members");
//                    if(members.containsKey("transactions")) {
//                        JSONArray transactions = members.getJSONArray("transactions");
//                        if(transactions.size() > 0) {
//                            JSONObject transactionsJson = (JSONObject) transactions.get(0);
//                            if(transactionsJson.containsKey("smses")) {
//                                JSONArray smses = transactionsJson.getJSONArray("smses");
//                                int n = smses.size();
//                                for (int i = 0; i < n; i++) {
//                                    JSONObject smsesJson = (JSONObject) smses.get(i);
//                                    if(!smsesJson.containsKey("other_cell_phone")) {
//                                        System.out.println(smsesJson.toJSONString());
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            log.info("原始运营商报告数据:{}", report == null?null:report.toJSONString());
//        } catch (Exception e) {
//            log.error("获取原始运营商报告数据出错", e);
//        }
//    }
//
//    @Test
//    public void test12() {
//        JSONObject report = null;
//        String orderNo="1667131333849079808";
//        try {
//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put("order_no", orderNo);
//            jsonObject1.put("type", "1");
//            for (int times = 0; times < 10 && report == null; times++) {
//                String result = RongZeRequestUtil.doPost(Constant.rongZeQueryUrl, "api.charge.data", jsonObject1.toJSONString());
////                log.warn("原始运营商报告数据的融泽返回结果："+result);
//                //判断运营商数据
//                JSONObject jsonObject = JSONObject.parseObject(result);
//                if (jsonObject.containsKey("data")) {
//                    String dataStr = jsonObject.getString("data");
//                    JSONObject all = JSONObject.parseObject(dataStr);
//                    if (all.containsKey("data")) {
//                        JSONObject data = all.getJSONObject("data");
//                        if (data.containsKey("report")) {
//                            report = data.getJSONObject("report");
//                        }
//                    }
//                }
//                log.info(orderNo + "原始运营商报告数据当前获取运营报告循环次数:{}", times);
//            }
//            if (report != null) {
//                if (report.containsKey("members")) {
//                    JSONObject members = report.getJSONObject("members");
//                    if (members.containsKey("transactions")) {
//                        JSONArray transactions = members.getJSONArray("transactions");
//                        if (transactions.size() > 0) {
//                            JSONObject transactionsJson = (JSONObject) transactions.get(0);
//                            if (transactionsJson.containsKey("smses")) {
//                                JSONArray smses = transactionsJson.getJSONArray("smses");
//                                int n = smses.size();
//                                for (int i = 0; i < n; i++) {
//                                    JSONObject smsesJson = (JSONObject) smses.get(i);
//                                    if(i == n-1) {
//                                        System.out.println("1");
//                                    }
//                                    if (!smsesJson.containsKey("other_cell_phone") || smsesJson.get("other_cell_phone") == null) {
//                                        System.out.println(i +"****" + smsesJson.toJSONString());
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
////            log.warn("原始运营商报告数据:{},{}", orderNo, report == null ? null : report.toJSONString());
//        } catch (Exception e) {
//            log.error(orderNo + "获取原始运营商报告数据出错", e);
//        }
//    }
//
//
//    @Test
//    public void putMqData2() {
//        OrderPayMessage message = new OrderPayMessage(10L);
//        rabbitTemplate.convertAndSend(RabbitConst.baofoo_queue_order_pay, message);
//    }
//
//
//    @Test
//    public void putMqData3() {
//        OrderPayMessage message = new OrderPayMessage(10L);
//        rabbitTemplate.convertAndSend(RabbitConst.kuaiqian_queue_order_pay, message);
//    }
//    @Test
//    public void report() {
//        JSONObject report = new JSONObject();
//        report.put("ss",null);
//        if(report.get("ss") == null){
//            System.out.println("寄哪里");
//        }
//    }
//
//}
