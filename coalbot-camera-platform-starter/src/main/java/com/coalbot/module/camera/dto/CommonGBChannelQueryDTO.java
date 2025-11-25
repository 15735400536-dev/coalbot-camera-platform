package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：CommonGBChannelQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/24 16:49
 * @Description: 国标通道分页查询DTO
 */
@Data
@Schema(description = "国标通道分页查询DTO")
public class CommonGBChannelQueryDTO extends SearchPage<CommonGBChannel> {

    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "是否在线")
    private Boolean online;
    @Schema(description = "是否已设置录制计划")
    private Boolean hasRecordPlan;
    @Schema(description = "通道类型，0：国标设备，1：推流设备，2：拉流代理")
    private Integer channelType;
    @Schema(description = "业务分组下的父节点ID")
    private String groupDeviceId;
    @Schema(description = "行政区划")
    private String civilCode;
    @Schema(description = "父节点编码")
    private String parentDeviceId;

}
