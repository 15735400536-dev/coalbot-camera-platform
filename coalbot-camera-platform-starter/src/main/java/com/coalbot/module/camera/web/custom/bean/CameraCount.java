package com.coalbot.module.camera.web.custom.bean;

import lombok.Data;

@Data
public class CameraCount {

    private String groupAlias;
    private String deviceId;
    private Long allCount;
    private Long onlineCount;

}
