package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.jt1078.bean.JTDevice;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：JTDeviceQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/20 16:29
 * @Description: 部标设备分页查询DTO
 */
@Data
@Schema(description = "部标设备分页查询DTO")
public class JTDeviceQueryDTO extends SearchPage<JTDevice> {

    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "是否在线")
    private Boolean online;

}
