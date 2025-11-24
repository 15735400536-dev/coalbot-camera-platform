package com.coalbot.module.camera.gb28181.controller.bean;

import lombok.Data;

import java.util.List;

@Data
public class ChannelToGroupParam {

    private String parentId;
    private String businessGroup;
    private List<String> channelIds;

}
