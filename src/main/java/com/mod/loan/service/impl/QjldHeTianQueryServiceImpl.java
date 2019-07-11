package com.mod.loan.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mod.loan.util.qjldUtil.QjldHeTianHelper;
import com.mod.loan.config.qjld.QjldConfig;
import com.mod.loan.model.DTO.QjldHeTianBaseReqDTO;
import com.mod.loan.model.DTO.QjldHeTianReqDTO;
import com.mod.loan.model.DTO.QjldHeTianResDTO;
import com.mod.loan.model.User;
import com.mod.loan.model.UserBank;
import com.mod.loan.service.QjldHeTianQueryService;
import com.mod.loan.util.ConstantUtils;
import com.mod.loan.util.MD5;
import com.mod.loan.util.RsaCodingUtil;
import com.mod.loan.util.RsaReadUtil;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * loan-risk 2019/4/29 huijin.shuailijie Init
 */
@Slf4j
@Service
public class QjldHeTianQueryServiceImpl implements QjldHeTianQueryService {

    public static final String CHAR_SET = "UTF-8";
    @Autowired
    private QjldHeTianHelper qjldHeTianHelper;
    @Autowired
    private QjldConfig qjldConfig;


    @Override
    public QjldHeTianResDTO qjldHeTianQuery(String transId, User user, UserBank userBank) {
        QjldHeTianBaseReqDTO reqDTO = new QjldHeTianBaseReqDTO();
        reqDTO.setMember_id(qjldConfig.getQjldMemberId());
        reqDTO.setTerminal_id(qjldConfig.getQjldTerminalId());
        reqDTO.setData_type(ConstantUtils.JSON_TYPE);
        String dataContent = getDataContent(user, userBank, transId);
        reqDTO.setData_content(dataContent);

        QjldHeTianResDTO qjldHeTianQuery = qjldHeTianHelper.qjldHeTianQuery(reqDTO);
        log.info("和添查询执行完成,响应信息:{}", qjldHeTianQuery);

        return qjldHeTianQuery;
    }

    /***
     * 加密参数请求
     *
     * @return 加密数据
     */
    private String getDataContent(User user, UserBank userBank, String orderId) {

        QjldHeTianReqDTO reqDTO = new QjldHeTianReqDTO();
        //添加客户基本信息(姓名身份证必填)
        reqDTO.setMember_id(qjldConfig.getQjldMemberId());
        reqDTO.setTerminal_id(qjldConfig.getQjldTerminalId());
        reqDTO.setTrans_id(orderId);
        reqDTO.setId_name(MD5.toMD5(user.getUserName()).toLowerCase());
        reqDTO.setId_no(MD5.toMD5(user.getUserCertNo()).toLowerCase());
        reqDTO.setProduct_type(qjldConfig.getQjldProductType());
        reqDTO.setTrade_date(new DateTime().toString("yyyyMMddHHmmss"));
        reqDTO.setVersions(qjldConfig.getQjldVersion());

        //添加自定义标签信息(该信息可以是map也可以是对象只要满足对应的json格式就可以)
        //没有自定义标签的可以不传这个参数
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = null;
        try {
            jsonData = objectMapper.writeValueAsString(reqDTO);
            log.info("请求JSON数据:{}", jsonData);
            jsonData = RsaCodingUtil.encryptByPrivateKey(Base64.encode(jsonData.getBytes(CHAR_SET)),
                    RsaReadUtil.getPrivateKeyFromFile(qjldConfig.getQjldKeyPath(), qjldConfig.getQjldKeyPwd()));

        } catch (Exception e) {
            log.error("转换字符串异常:{}", e);
        }

        return jsonData;
    }
}
