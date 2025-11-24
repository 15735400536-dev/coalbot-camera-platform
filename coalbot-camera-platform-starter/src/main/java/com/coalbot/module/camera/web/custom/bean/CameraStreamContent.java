package com.coalbot.module.camera.web.custom.bean;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.vmanager.bean.StreamContent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CameraStreamContent extends StreamContent {

    public CameraStreamContent(StreamInfo streamInfo) {
        super(streamInfo);
    }

    private String name;

    // 0不可动，1可动
    private Integer controlType;


}
