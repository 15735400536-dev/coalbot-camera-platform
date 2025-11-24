package com.coalbot.module.camera.web.custom.bean;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CameraStreamInfo {


    private CommonGBChannel channel;


    private StreamInfo streamInfo;

    public CameraStreamInfo(CommonGBChannel channel, StreamInfo streamInfo) {
        this.channel = channel;
        this.streamInfo = streamInfo;
    }
}
