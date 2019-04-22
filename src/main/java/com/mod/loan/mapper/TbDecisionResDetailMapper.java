package com.mod.loan.mapper;

import com.mod.loan.model.TbDecisionResDetail;
import tk.mybatis.mapper.common.Mapper;

public interface TbDecisionResDetailMapper extends Mapper<TbDecisionResDetail> {
    Integer updateByTransId(TbDecisionResDetail tbDecisionResDetail);

    TbDecisionResDetail selectByTransId(String transId);
}