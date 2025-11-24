package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "录制计划关联的所有通道分页查询DTO")
public class ChannelQueryDTO extends SearchPage<CommonGBChannel> {

    @Schema(description = "录制计划ID")
    private String planId;
    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "通道类型， 0：国标设备，1：推流设备，2：拉流代理")
    private Integer channelType;
    @Schema(description = "是否在线")
    private Boolean online;
    @Schema(description = "是否已经关联")
    private Boolean hasLink;

}
