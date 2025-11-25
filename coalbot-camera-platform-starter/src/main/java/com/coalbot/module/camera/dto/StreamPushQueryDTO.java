package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.streamPush.bean.StreamPush;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：StreamPushQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/19 15:10
 * @Description: 推流列表分页查询DTO
 */
@Data
@Schema(description = "推流列表分页查询DTO")
public class StreamPushQueryDTO extends SearchPage<StreamPush> {

    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "是否正在推流")
    private Boolean pushing;
    @Schema(description = "流媒体ID")
    private String mediaServerId;

}
