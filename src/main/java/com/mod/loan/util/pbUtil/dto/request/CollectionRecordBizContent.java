package com.mod.loan.util.pbUtil.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class CollectionRecordBizContent {
    /**
     * 进件订单号
     */
    @NotBlank(message = "进件订单号不能为空")
    private String loanNo;

    /**
     * 期次
     */
    @NotNull(message = "期次不能为空")
    private Integer loanRepaymentIndexs;
    /**
     * 催收时间
     */
    @NotBlank(message = "催收时间不能为空")
    private String collectionDate;
    /**
     * 催收总状态:0催收成功 1.承诺还款 2.电话躲避 3.本人失联 4.三方失联 5.部分还款 6.资金不足 7.失去联系 8.已提交外包 9.敷衍跳票 10. 拒不还款 11 其他
     */
    @NotBlank(message = "催收总状态不能为空")
    private String result;
    /**
     * 催收详情
     */
    private List<CollectionDetailDto> collDetail;

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public Integer getLoanRepaymentIndexs() {
        return loanRepaymentIndexs;
    }

    public void setLoanRepaymentIndexs(Integer loanRepaymentIndexs) {
        this.loanRepaymentIndexs = loanRepaymentIndexs;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        this.collectionDate = collectionDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<CollectionDetailDto> getCollDetail() {
        return collDetail;
    }

    public void setCollDetail(List<CollectionDetailDto> collDetail) {
        this.collDetail = collDetail;
    }
}