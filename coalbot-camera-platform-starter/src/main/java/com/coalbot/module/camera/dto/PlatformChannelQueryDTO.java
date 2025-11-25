package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.PlatformChannel;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：PlatformChannelQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/19 14:58
 * @Description: 级联平台的所有通道分页查询DTO
 */
@Data
@Schema(description = "级联平台的所有通道分页查询DTO")
public class PlatformChannelQueryDTO extends SearchPage<PlatformChannel> {

    @Schema(description = "上级平台的数据ID")
    private String platformId;
    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "通道类型， 0：国标设备，1：推流设备，2：拉流代理")
    private Integer channelType;
    @Schema(description = "是否在线")
    private Boolean online;
    @Schema(description = "是否已经共享")
    private Boolean hasShare;

}
