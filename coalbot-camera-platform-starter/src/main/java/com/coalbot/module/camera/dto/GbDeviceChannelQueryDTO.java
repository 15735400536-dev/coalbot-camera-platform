package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.DeviceChannel;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：GbDeviceChannelQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/19 14:53
 * @Description: 国标通道分页查询DTO
 */
@Data
@Schema(description = "国标通道分页查询DTO")
public class GbDeviceChannelQueryDTO extends SearchPage<DeviceChannel> {

    @Schema(description = "设备国标编号")
    private String deviceId;
    @Schema(description = "通道国标编号")
    private String channelId;
    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "是否在线")
    private Boolean online;
    @Schema(description = "设备/子目录-> false/true")
    private Boolean channelType;

}
