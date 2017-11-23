package com.docomo.Data;

public class TestStatus {

    public static final int FirstBoot = 0x0;// 第一次启动
    public static final int gettingLatency = 0x1;// 获取ping阶段
    public static final int gettingUploadSpeed = 0x2;// 获取上行速度阶段
    public static final int gettingDownloadSpeed = 0x4;// 获取下行速度阶段
    public static final int oneTestCompleted = 0x8;// 测试结束阶段
    public static final int reSetServer = 0x16;// 重设服务器地址
    /**
     * 异常中断阶段
     * */
    public static final int testInterruptInlatency = 0x46;// 异常中断阶段
    public static final int testInterruptInupload = 0x47;// 异常中断阶段
    public static final int testInterruptIndownload = 0x48;// 异常中断阶段

    public static int testingStatus = FirstBoot;
    public static boolean isFirstTimeBoot = true;

    /**
     * 测试配置参数
     * 
     * */
    public static final String Market = "jifeng";
    //con_version active test engine version ,  SDK_version passive test engine version 
    public static final String Con_version = "ANTTEST6.3.4-" + Market;// 版本+市场
    public static String locationTag = "";
    public static int languageType = 0;
    public static boolean isLanguageChanged = false;
    /**
     * 虚拟版本号设置 0:主版本号 1：校园测试版 2：高级模式版本
     * */
    public static int SettingVersion = 2;
    public static final int SETTING_MAIN_VERSION = 0;
    public static final int SETTING_CAMPUS_VERSION = 1;
    public static final int SETTING_ADVANCED_VERSION = 2;

    /**
     * 测试数据是否即时上传,为true时即时上传，为false时则存储在本地，wifi情况下上传
     * */
    public static boolean isUploadInstant = true;

}
