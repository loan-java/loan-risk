package com.mod.loan.service.impl;

import com.alibaba.fastjson.JSON;
import com.mod.loan.common.enums.PbResultEnum;
import com.mod.loan.common.enums.PolicyResultEnum;
import com.mod.loan.config.Constant;
import com.mod.loan.mapper.MerchantRateMapper;
import com.mod.loan.model.MerchantRate;
import com.mod.loan.model.OrderUser;
import com.mod.loan.service.CallBackRongZeService;
import com.mod.loan.util.rongze.RongZeRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class CallBackRongZeServiceImpl implements CallBackRongZeService {

    @Resource
    private MerchantRateMapper merchantRateMapper;

    @Override
    public void pushRiskResultForQjld(OrderUser orderUser, String riskCode, String riskDesc) {
        try {
            //只推审核通过跟审核拒绝
            if (!(PolicyResultEnum.isAgree(riskCode) || PolicyResultEnum.isReject(riskCode))) {
                return;
            }
            if (orderUser == null || orderUser.getOrderNo() == null) {
                return;
            }
            //推审批结
            postRiskResultForQjld(orderUser, riskCode, riskDesc);

        } catch (Exception e) {
            log.error("给融泽推送风控审批结果失败: " + e.getMessage(), e);
        }
    }


    @Override
    public void pushRiskResultForPb(OrderUser orderUser, String riskCode, String riskDesc) {
        try {
            //只推审核通过跟审核拒绝
            if (!(PbResultEnum.isAPPROVE(riskCode) || PbResultEnum.isDENY(riskCode)|| PbResultEnum.isMANUAL(riskCode))) {
                return;
            }
            if (orderUser == null || orderUser.getOrderNo() == null) {
                return;
            }
            //推审批结
            postRiskResultForPb(orderUser, riskCode, riskDesc);

        } catch (Exception e) {
            log.error("给融泽推送风控审批结果失败: " + e.getMessage(), e);
        }
    }

    private void postRiskResultForQjld(OrderUser orderUser, String riskCode, String riskDesc) throws Exception {
        String orderNo = orderUser.getOrderNo();

        String reapply = null; //是否可再次申请
        String reapplyTime = null; //可再申请的时间
        String remark = "审批中"; //拒绝原因

        Long refuseTime = null; //审批拒绝时间
        Long approvalTime = null; //审批通过时间
        int conclusion = 30; //处理中
        Integer proType = null; //单期产品
        Integer amountType = null; //审批金额是否固定，0 - 固定
        Integer termType = null; //审批期限是否固定，0 - 固定
        Integer approvalAmount = null; //审批金额
        Integer approvalTerm = null; //审批期限
        Integer termUnit = null; //期限单位，1 - 天
        String creditDeadline = null; //审批结果有效期，当前时间

        if (PolicyResultEnum.isAgree(riskCode)) {
            //是否存在关联的借贷信息
            if(orderUser.getMerchantRateId() == null){
                throw new Exception("审批结论推送:商户不存在Order关联的借贷信息" + orderUser.toString());
            }
            MerchantRate merchantRate = merchantRateMapper.selectByPrimaryKey(orderUser.getMerchantRateId());
            if(merchantRate == null){
                throw new Exception("审批结论推送:商户不存在默认的借贷信息");
            }
            BigDecimal approvalAmount1 = merchantRate.getProductMoney(); //审批金额
            if(approvalAmount1 == null) {
                throw new Exception("审批结论推送:商户不存在默认借贷金额" + merchantRate.toString());
            }
            Integer approvalTerm1 = merchantRate.getProductDay(); //审批期限
            if(approvalTerm1 == null) {
                throw new Exception("审批结论推送:商户不存在默认借贷期限" + merchantRate.toString());
            }
            approvalAmount = approvalAmount1.intValue(); //审批金额
            approvalTerm = approvalTerm1.intValue(); //审批期限

            //通过
            conclusion = 10;
            approvalTime = System.currentTimeMillis();
            creditDeadline = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            proType = 1; //单期产品
            amountType = 0; //审批金额是否固定，0 - 固定
            termType = 0; //审批期限是否固定，0 - 固定
            termUnit = 1; //期限单位，1 - 天
            remark = "通过";
        } else {
            //拒绝
            refuseTime = System.currentTimeMillis();
            conclusion = 40;
            remark = StringUtils.isNotBlank(riskDesc) ? riskDesc : "拒绝";
            reapply = "1";
            reapplyTime = DateFormatUtils.format(refuseTime + (1000L * 3600 * 24 * 7), "yyyy-MM-dd");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("order_no", orderNo);
        map.put("conclusion", conclusion);
        map.put("reapply", reapply);
        map.put("reapplytime", reapplyTime);
        map.put("remark", remark);
        map.put("refuse_time", refuseTime);
        map.put("approval_time", approvalTime);
        map.put("pro_type", proType);
        map.put("term_unit", termUnit);
        map.put("amount_type", amountType);
        map.put("term_type", termType);
        map.put("approval_term", approvalTerm);
        map.put("credit_deadline", creditDeadline);
        map.put("approval_amount", approvalAmount);
        RongZeRequestUtil.doPost(Constant.rongZeCallbackUrl, "api.audit.result", JSON.toJSONString(map));
    }




    private void postRiskResultForPb(OrderUser orderUser, String riskCode, String riskDesc) throws Exception {
        String orderNo = orderUser.getOrderNo();

        String reapply = null; //是否可再次申请
        String reapplyTime = null; //可再申请的时间
        String remark = "审批中"; //拒绝原因

        Long refuseTime = null; //审批拒绝时间
        Long approvalTime = null; //审批通过时间
        int conclusion = 30; //处理中
        Integer proType = null; //单期产品
        Integer amountType = null; //审批金额是否固定，0 - 固定
        Integer termType = null; //审批期限是否固定，0 - 固定
        Integer approvalAmount = null; //审批金额
        Integer approvalTerm = null; //审批期限
        Integer termUnit = null; //期限单位，1 - 天
        String creditDeadline = null; //审批结果有效期，当前时间

        if (PbResultEnum.isAPPROVE(riskCode)) {
            //是否存在关联的借贷信息
            if(orderUser.getMerchantRateId() == null){
                throw new Exception("审批结论推送:商户不存在Order关联的借贷信息" + orderUser.toString());
            }
            MerchantRate merchantRate = merchantRateMapper.selectByPrimaryKey(orderUser.getMerchantRateId());
            if(merchantRate == null){
                throw new Exception("审批结论推送:商户不存在默认的借贷信息");
            }
            BigDecimal approvalAmount1 = merchantRate.getProductMoney(); //审批金额
            if(approvalAmount1 == null) {
                throw new Exception("审批结论推送:商户不存在默认借贷金额" + merchantRate.toString());
            }
            Integer approvalTerm1 = merchantRate.getProductDay(); //审批期限
            if(approvalTerm1 == null) {
                throw new Exception("审批结论推送:商户不存在默认借贷期限" + merchantRate.toString());
            }
            approvalAmount = approvalAmount1.intValue(); //审批金额
            approvalTerm = approvalTerm1.intValue(); //审批期限

            //通过
            conclusion = 10;
            approvalTime = System.currentTimeMillis();
            creditDeadline = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            proType = 1; //单期产品
            amountType = 0; //审批金额是否固定，0 - 固定
            termType = 0; //审批期限是否固定，0 - 固定
            termUnit = 1; //期限单位，1 - 天
            remark = "通过";
        } else {
            //拒绝
            refuseTime = System.currentTimeMillis();
            conclusion = 40;
            remark = StringUtils.isNotBlank(riskDesc) ? riskDesc : "拒绝";
            reapply = "1";
            reapplyTime = DateFormatUtils.format(refuseTime + (1000L * 3600 * 24 * 7), "yyyy-MM-dd");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("order_no", orderNo);
        map.put("conclusion", conclusion);
        map.put("reapply", reapply);
        map.put("reapplytime", reapplyTime);
        map.put("remark", remark);
        map.put("refuse_time", refuseTime);
        map.put("approval_time", approvalTime);
        map.put("pro_type", proType);
        map.put("term_unit", termUnit);
        map.put("amount_type", amountType);
        map.put("term_type", termType);
        map.put("approval_term", approvalTerm);
        map.put("credit_deadline", creditDeadline);
        map.put("approval_amount", approvalAmount);
        RongZeRequestUtil.doPost(Constant.rongZeCallbackUrl, "api.audit.result", JSON.toJSONString(map));
    }

    private void postOrderStatus(Map<String, Object> map) throws Exception {
        RongZeRequestUtil.doPost(Constant.rongZeCallbackUrl, "api.order.status", JSON.toJSONString(map));
    }


    public void pushRiskResultForZm(OrderUser orderUser, String riskCodec) {
        try {
            if (orderUser == null || orderUser.getOrderNo() == null) {
                return;
            }
            //推审批结
            postRiskResultForZm(orderUser, riskCodec);

        } catch (Exception e) {
            log.error("给融泽推送风控审批结果失败: " + e.getMessage(), e);
        }
    }
    private void postRiskResultForZm(OrderUser orderUser, String riskCode) throws Exception {
        String orderNo = orderUser.getOrderNo();

        String reapply = null; //是否可再次申请
        String reapplyTime = null; //可再申请的时间
        String remark = "审批中"; //拒绝原因

        Long refuseTime = null; //审批拒绝时间
        Long approvalTime = null; //审批通过时间
        int conclusion = 30; //处理中
        Integer proType = null; //单期产品
        Integer amountType = null; //审批金额是否固定，0 - 固定
        Integer termType = null; //审批期限是否固定，0 - 固定
        Integer approvalAmount = null; //审批金额
        Integer approvalTerm = null; //审批期限
        Integer termUnit = null; //期限单位，1 - 天
        String creditDeadline = null; //审批结果有效期，当前时间

        if ("0".equals(riskCode)) {
            //是否存在关联的借贷信息
            if(orderUser.getMerchantRateId() == null){
                throw new Exception("审批结论推送:商户不存在Order关联的借贷信息" + orderUser.toString());
            }
            MerchantRate merchantRate = merchantRateMapper.selectByPrimaryKey(orderUser.getMerchantRateId());
            if(merchantRate == null){
                throw new Exception("审批结论推送:商户不存在默认的借贷信息");
            }
            BigDecimal approvalAmount1 = merchantRate.getProductMoney(); //审批金额
            if(approvalAmount1 == null) {
                throw new Exception("审批结论推送:商户不存在默认借贷金额" + merchantRate.toString());
            }
            Integer approvalTerm1 = merchantRate.getProductDay(); //审批期限
            if(approvalTerm1 == null) {
                throw new Exception("审批结论推送:商户不存在默认借贷期限" + merchantRate.toString());
            }
            approvalAmount = approvalAmount1.intValue(); //审批金额
            approvalTerm = approvalTerm1.intValue(); //审批期限

            //通过
            conclusion = 10;
            approvalTime = System.currentTimeMillis();
            creditDeadline = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
            proType = 1; //单期产品
            amountType = 0; //审批金额是否固定，0 - 固定
            termType = 0; //审批期限是否固定，0 - 固定
            termUnit = 1; //期限单位，1 - 天
            remark = "通过";
        } else {
            //拒绝
            refuseTime = System.currentTimeMillis();
            conclusion = 40;
            remark = "拒绝" ;
            reapply = "1";
            reapplyTime = DateFormatUtils.format(refuseTime + (1000L * 3600 * 24 * 7), "yyyy-MM-dd");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("order_no", orderNo);
        map.put("conclusion", conclusion);
        map.put("reapply", reapply);
        map.put("reapplytime", reapplyTime);
        map.put("remark", remark);
        map.put("refuse_time", refuseTime);
        map.put("approval_time", approvalTime);
        map.put("pro_type", proType);
        map.put("term_unit", termUnit);
        map.put("amount_type", amountType);
        map.put("term_type", termType);
        map.put("approval_term", approvalTerm);
        map.put("credit_deadline", creditDeadline);
        map.put("approval_amount", approvalAmount);
        RongZeRequestUtil.doPost(Constant.rongZeCallbackUrl, "api.audit.result", JSON.toJSONString(map));
    }

}
