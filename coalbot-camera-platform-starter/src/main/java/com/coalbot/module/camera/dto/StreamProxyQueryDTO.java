package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：StreamProxyQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/20 16:14
 * @Description: 拉流代理分页查询DTO
 */
@Data
@Schema(description = "拉流代理分页查询DTO")
public class StreamProxyQueryDTO extends SearchPage<StreamProxy> {

    @Schema(description = "查询内容")
    private String query;
    @Schema(description = "是否正在拉流")
    private Boolean pulling;
    @Schema(description = "流媒体ID")
    private String mediaServerId;

}
