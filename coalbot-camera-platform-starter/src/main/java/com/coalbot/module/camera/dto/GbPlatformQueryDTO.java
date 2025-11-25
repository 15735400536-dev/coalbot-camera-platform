package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.Platform;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：GbPlatformQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/10/31 15:15
 * @Description: 国标平台分页查询DTO
 */
@Data
@Schema(description = "国标平台分页查询DTO")
public class GbPlatformQueryDTO extends SearchPage<Platform> {

    @Schema(description = "查询内容")
    private String query;

}
