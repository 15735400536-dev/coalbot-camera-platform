package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.web.custom.bean.CameraChannel;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：CameraChannelQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/27 10:16
 * @Description: 摄像头通道分页查询DTO
 */
@Data
@Schema(description = "摄像头通道分页查询DTO")
public class CameraChannelQueryDTO extends SearchPage<CameraChannel> {

    @Schema(description = "要搜索的内容")
    private String query;
    @Schema(description = "分组别名")
    private String groupAlias;
    @Schema(description = "坐标系类型：WGS84,GCJ02、BD09")
    private String geoCoordSys;
    @Schema(description = "摄像头状态")
    private Boolean status;
    @Schema(description = "排序字段名")
    private String sortName;
    @Schema(description = "排序方式（true: 升序 或 false: 降序 ）")
    private Boolean sort;
    @Schema(description = "分组别名")
    private String topGroupAlias;

}
