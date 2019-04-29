package com.mod.loan.api;

import com.mod.loan.model.DTO.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * 决策引擎api
 *
 * @author lijing
 * @date 2017/12/27 0027.
 */
public interface DecisionEngineApi {


    /**v
     * 全景雷达(和添)
     *
     * @param reqDTO 请求参数
     * @return 响应结果
     */
    @POST("/product/credit/v1/unify")
    Call<EngineResult<DecisionBaseResDTO>> qjldQuery(@Body QjldHeTianBaseReqDTO reqDTO);


    /**
     * 同步请求
     *
     * @param reqDTO 请求参数
     * @return 响应结果
     */
    @POST("/credit-decision-engine/decision/custom/v2/execute")
    Call<EngineResult<DecisionBaseResDTO>> syncDecision(@Body DecisionBaseReqDTO reqDTO);

    /**
     * 异步请求
     *
     * @param reqDTO 请求参数
     * @return 响应结果
     */
    @POST("/credit-decision-engine/decision/custom/v3/execute")
    Call<EngineResult<DecisionBaseResDTO>> nosyncDecision(@Body DecisionBaseReqDTO reqDTO);


    @POST("/credit-decision-engine/decision/queryExecute")
    Call<EngineResult<DecisionResDetailDTO>> queryDecision(@Body DecisionReqDTO reqDTO);

}
