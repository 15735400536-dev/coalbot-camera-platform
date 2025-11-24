package com.coalbot.module.camera.gb28181.controller.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelListForRpcParam {
    private List<String> channelIds;
    private String platformId;

}
