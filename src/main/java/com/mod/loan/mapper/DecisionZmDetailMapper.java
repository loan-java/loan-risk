package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.DecisionPbDetail;
import com.mod.loan.model.DecisionZmDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DecisionZmDetailMapper extends MyBaseMapper<DecisionZmDetail> {

    @Select("select id,order_no as orderNo,return_code as returnCode,create_time as createTime from tb_decision_zm_detail where order_no=#{orderNo} ")
    DecisionZmDetail selectByOrderNo(@Param("orderNo") String orderNo);

}