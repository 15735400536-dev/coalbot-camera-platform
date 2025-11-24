package com.coalbot.module.camera.gb28181.controller.bean;

import lombok.Data;

import java.util.List;

@Data
public class ChannelToGroupByGbDeviceParam {
    private List<String> deviceIds;
    private String parentId;
    private String businessGroup;
}
