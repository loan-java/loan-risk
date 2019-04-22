package com.mod.loan.model;

import com.mod.loan.model.DTO.DecisionResDetailDTO;

import javax.persistence.*;
import java.util.Date;

@Table(name = "tb_decision_res_detail")
public class TbDecisionResDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long order_id;

    @Column(name = "decision_no")
    private String decisionNo;

    @Column(name = "trans_id")
    private String transId;

    @Column(name = "orderStatus")
    private String orderstatus;

    @Column(name = "order_money")
    private Long orderMoney;

    @Column(name = "fee")
    private Boolean fee;

    @Column(name = "custom_grade")
    private String customGrade;

    @Column(name = "code")
    private String code;

    @Column(name = "descs")
    private String descs;

    @Column(name = "resScore")
    private Double resscore;

    @Column(name = "strategies")
    private String strategies;

    @Column(name = "createTime")
    private Date createtime;

    @Column(name = "updateTime")
    private Date updatetime;


    /**
     * @return decision_no
     */
    public String getDecisionNo() {
        return decisionNo;
    }

    /**
     * @param decisionNo
     */
    public void setDecisionNo(String decisionNo) {
        this.decisionNo = decisionNo;
    }

    /**
     * @return trans_id
     */
    public String getTransId() {
        return transId;
    }

    /**
     * @param transId
     */
    public void setTransId(String transId) {
        this.transId = transId;
    }

    /**
     * @return orderStatus
     */
    public String getOrderstatus() {
        return orderstatus;
    }

    /**
     * @param orderstatus
     */
    public void setOrderstatus(String orderstatus) {
        this.orderstatus = orderstatus;
    }

    /**
     * @return order_money
     */
    public Long getOrderMoney() {
        return orderMoney;
    }

    /**
     * @param orderMoney
     */
    public void setOrderMoney(Long orderMoney) {
        this.orderMoney = orderMoney;
    }

    /**
     * @return fee
     */
    public Boolean getFee() {
        return fee;
    }

    /**
     * @param fee
     */
    public void setFee(Boolean fee) {
        this.fee = fee;
    }

    /**
     * @return custom_grade
     */
    public String getCustomGrade() {
        return customGrade;
    }

    /**
     * @param customGrade
     */
    public void setCustomGrade(String customGrade) {
        this.customGrade = customGrade;
    }

    /**
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return desc
     */
    public String getDescs() {
        return descs;
    }

    /**
     * @param descs
     */
    public void setDescs(String descs) {
        this.descs = descs;
    }

    /**
     * @return resScore
     */
    public Double getResscore() {
        return resscore;
    }

    /**
     * @param resscore
     */
    public void setResscore(Double resscore) {
        this.resscore = resscore;
    }

    /**
     * @return strategies
     */
    public String getStrategies() {
        return strategies;
    }

    /**
     * @param strategies
     */
    public void setStrategies(String strategies) {
        this.strategies = strategies;
    }

    /**
     * @return createTime
     */
    public Date getCreatetime() {
        return createtime;
    }

    /**
     * @param createtime
     */
    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    /**
     * @return updateTime
     */
    public Date getUpdatetime() {
        return updatetime;
    }

    /**
     * @param updatetime
     */
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }

    public TbDecisionResDetail(DecisionResDetailDTO decisionResDetailDTO) {
        this.decisionNo = decisionResDetailDTO.getDecision_no();

        this.transId = decisionResDetailDTO.getTrans_id();

        this.orderstatus = decisionResDetailDTO.getOrderStatus();

        this.orderMoney = decisionResDetailDTO.getOrder_money();

        this.fee = decisionResDetailDTO.getFee();

        this.customGrade = decisionResDetailDTO.getCustom_grade().toString();

        this.code = decisionResDetailDTO.getCode();

        this.descs = decisionResDetailDTO.getDesc();

        this.resscore = decisionResDetailDTO.getResScore();

        this.strategies = decisionResDetailDTO.getStrategies().toString();
    }


    public TbDecisionResDetail(Long order_id, String decisionNo, String transId, String orderstatus) {
        this.setOrder_id(order_id);
        this.setDecisionNo(decisionNo);
        this.setTransId(transId);
        this.setOrderstatus(orderstatus);
    }
}