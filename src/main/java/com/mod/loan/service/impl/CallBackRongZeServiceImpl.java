package com.mod.loan.service.impl;

import com.mod.loan.config.Constant;
import com.mod.loan.model.Order;
import com.mod.loan.service.CallBackRongZeService;
import com.mod.loan.util.rongze.RongZeRequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CallBackRongZeServiceImpl implements CallBackRongZeService {

    @Override
    public void pushOrderStatus(Order order) {
        try {
            RongZeRequestUtil.doPost(Constant.rongZeCallbackUrl, "api.order.status", "");
        } catch (Exception e) {
            log.error("给融泽推送订单状态失败: " + e.getMessage(), e);
        }
    }
}
