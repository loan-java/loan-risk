package com.mod.loan.util.pbUtil;


import com.mod.loan.util.pbUtil.utils.constant.SlpConstant;

public class SlpPanbaoBuilder {


    private SlpPanbaoBuilder(){}

    private PanbaoClient panbaoClient;

    private void setPanbaoClient(PanbaoClient panbaoClient) {
        this.panbaoClient = panbaoClient;
    }

    public static SlpPanbaoBuilder build(String merchantId, String privateKey){
        SlpPanbaoBuilder client = new SlpPanbaoBuilder();
        client.setPanbaoClient(new PanbaoClient(merchantId, privateKey));
        return client;
    }

    public PanbaoClient pro(){
        panbaoClient.setHost( SlpConstant.ONLINE_HOST);
        panbaoClient.setPlatformPubKey(SlpConstant.ONLINE_PLATPUBKEY);
        return panbaoClient;
    }

    public PanbaoClient test(){
        panbaoClient.setHost( SlpConstant.TEST_HOST);
        panbaoClient.setPlatformPubKey(SlpConstant.TEST_PLATPUBKEY);
        return panbaoClient;
    }

}
