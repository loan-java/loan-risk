package com.mod.loan.controller;

import com.mod.loan.model.DTO.DecisionBaseResDTO;
import com.mod.loan.model.DTO.EngineResult;
import com.mod.loan.model.DTO.ManualAuditDTO;
import com.mod.loan.util.ConstantUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
@RequestMapping("/qjldCallBack")
@RestController
public class QjldCallBackController {


    @RequestMapping(value = "/policyCallBack", method = RequestMethod.POST)
    public String QjldPolicyCallBack(@RequestBody EngineResult<DecisionBaseResDTO> engineResult) {
        System.out.println(engineResult.getData());
        return ConstantUtils.OK;
    }

    @RequestMapping(value = "/manualAuditCallBack", method = RequestMethod.POST)
    public String QjldManualAuditCallBack(@RequestBody ManualAuditDTO manualAuditDTO) {
        System.out.println(manualAuditDTO.toString());
        return ConstantUtils.OK;
    }


}
