package com.coalbot.module.camera.common.enums;

/**
 * 支持的通道数据类型
 */

public class ChannelDataType {

    public final static int GB28181 = 1;
    public final static int STREAM_PUSH = 2;
    public final static int STREAM_PROXY = 3;
    public final static int JT_1078 = 200;

    public final static String PLAY_SERVICE = "sourceChannelPlayService";
    public final static String PLAYBACK_SERVICE = "sourceChannelPlaybackService";
    public final static String DOWNLOAD_SERVICE = "sourceChannelDownloadService";
    public final static String PTZ_SERVICE = "sourceChannelPTZService";


    public static String getDateTypeDesc(Integer dataType) {
        if (dataType == null) {
            return "未知";
        }
        switch (dataType) {
            case GB28181:
                return "国标28181";
            case STREAM_PUSH:
                return "推流设备";
            case STREAM_PROXY:
                return "拉流代理";
            case JT_1078:
                return "部标设备";
            default:
                return "未知";
        }
    }

}
