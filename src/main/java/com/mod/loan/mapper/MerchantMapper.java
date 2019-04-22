package com.mod.loan.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.Merchant;

public interface MerchantMapper extends MyBaseMapper<Merchant> {

	List<Merchant> selectMerchantAliasByStatus(@Param("status") int status);
}