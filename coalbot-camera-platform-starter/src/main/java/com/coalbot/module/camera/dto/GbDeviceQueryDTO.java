package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.Device;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：GbDeviceQuery
 * @Author: XinHai.Ma
 * @Date: 2025/11/19 14:49
 * @Description: 国标设备分页查询DTO
 */
@Data
@Schema(description = "国标设备分页查询DTO")
public class GbDeviceQueryDTO extends SearchPage<Device> {

    @Schema(description = "搜索")
    private String query;
    @Schema(description = "状态")
    private Boolean status;

}
