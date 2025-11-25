package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.jt1078.bean.JTChannel;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：JTChannelQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/20 16:36
 * @Description: 部标通道分页查询DTO
 */
@Data
@Schema(description = "部标通道分页查询DTO")
public class JTChannelQueryDTO extends SearchPage<JTChannel> {

    @Schema(description = "设备ID")
    private String deviceId;
    @Schema(description = "查询内容")
    private String query;

}
