package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.service.bean.CloudRecordItem;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：CloudRecordItemQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/11/24 16:45
 * @Description: 云端录像分页查询DTO
 */
@Data
@Schema(description = "云端录像分页查询DTO")
public class CloudRecordItemQueryDTO extends SearchPage<CloudRecordItem> {

    @Schema(description = "检索内容")
    private String query;
    @Schema(description = "应用名")
    private String app;
    @Schema(description = "流ID")
    private String stream;
    @Schema(description = "开始时间(yyyy-MM-dd HH:mm:ss)")
    private String startTime;
    @Schema(description = "结束时间(yyyy-MM-dd HH:mm:ss)")
    private String endTime;
    @Schema(description = "流媒体ID，置空则查询全部流媒体")
    private String mediaServerId;
    @Schema(description = "每次录像的唯一标识，置空则查询全部流媒体")
    private String callId;
    @Schema(description = "是否升序排序，升序：true，降序：false")
    private Boolean ascOrder;

}
