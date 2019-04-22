package com.mod.loan.mapper;

import com.mod.loan.common.mapper.MyBaseMapper;
import com.mod.loan.model.UserBank;
import org.apache.ibatis.annotations.Param;

public interface UserBankMapper extends MyBaseMapper<UserBank> {

	/**
	 * 获取当前使用中的银行卡
	 * 
	 * @param uid
	 * @return
	 */
	UserBank selectUserCurrentBankCard(@Param("uid") Long uid);

}