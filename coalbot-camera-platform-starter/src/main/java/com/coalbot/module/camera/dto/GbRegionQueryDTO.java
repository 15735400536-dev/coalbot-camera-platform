package com.coalbot.module.camera.dto;

import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.swing.plaf.synth.Region;

/**
 * @ClassName：GbRegionQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/19 15:02
 * @Description: 区域分页查询DTO
 */
@Data
@Schema(description = "区域分页查询DTO")
public class GbRegionQueryDTO extends SearchPage<Region> {

    @Schema(description = "要搜索的内容")
    private String query;

}
