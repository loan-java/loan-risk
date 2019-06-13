package com.mod.loan.util.pbUtil.utils.constant;

/**
 * 此对象为十露盘的相关接口数据，不可修改
 */
public class SlpConstant {

    /**
     * 生产地址前缀
     */
    public static final String ONLINE_HOST = "http://risk.slooptech.com/api/risk";
    /**
     * 测试地址前缀
     */
    public static final String TEST_HOST = "http://test-risk.slooptech.com/api/risk";
    public static final String creditApply = "/asset/loanApplyWithData";
    public static final String pushPlan = "/feedback/batchRepayPlan";
    public static final String pushRecord = "/feedback/batchRepayRecord";
    public static final String queryCreditResult = "/asset/queryRiskResult";
    public static final String pushCollectionRecord = "/feedback/collectionRecord";
    public static final String updateOrder = "/feedback/batchUpdateOrder";


    /**
     * 十露盘生产公钥（固定值，不用修改）
     */
    public static final String ONLINE_PLATPUBKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDG55a3wl9naBfJXLeKzaCWdpWUHeVGn38P1TEAgrTRlEiVRKB2PZ7KzjF3byNWO11HWmGNXGA6oLIi5LgzEdC8LYxD+6w9lMt64U0eyzHzeM5CXsoDIe8urwUIIJy7aErP/0OCGulD3PLOKXL0JbmHT5ZAzBMI+B/9OHkStE76hwIDAQAB";
    /**
     * 十露盘测试公钥（固定值，不用修改）
     */
    public static final String TEST_PLATPUBKEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCAqI2Trr9HEI0tbClxJWGFSVIk5zYksj7yf/oEoBrG4FUWkMso8Z5AQO14DFpBkYW54P9t1D0nIIUyP8jxvXrHzAmoYO+NPOX7wXMikEGgPpvjgPeENio5XrrbpF8PwuHTcD9fDKhkKLmf3hFhR7LE9Uh5/NUsvdbsovM/drb5cwIDAQAB";

}