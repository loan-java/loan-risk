package com.mod.loan.model.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * loan-pay 2019/4/22 huijin.shuailijie Init
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManualAuditDTO implements Serializable {

    @JsonProperty("auditStatus")
    private String auditStatus;

    @JsonProperty("auditor")
    private String auditor;


    @JsonProperty("auditMsg")
    private String auditMsg;


    @JsonProperty("decisionExecuteRes")
    private DecisionExecuteResDTO decisionExecuteRes;
}
