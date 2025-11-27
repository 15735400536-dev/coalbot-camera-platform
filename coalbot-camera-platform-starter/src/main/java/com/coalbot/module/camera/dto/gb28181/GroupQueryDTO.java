package com.coalbot.module.camera.dto.gb28181;

import com.coalbot.module.camera.gb28181.bean.Group;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：GroupQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/27 9:59
 * @Description: 分组分页查询DTO
 */
@Data
@Schema(description = "分组分页查询DTO")
public class GroupQueryDTO extends SearchPage<Group> {

    @Schema(description = "要搜索的内容")
    private String query;
    @Schema(description = "true为查询通道，false为查询节点")
    private boolean channel;

}
