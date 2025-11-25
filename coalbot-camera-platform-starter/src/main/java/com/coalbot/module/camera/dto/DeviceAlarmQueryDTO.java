package com.coalbot.module.camera.dto;

import com.coalbot.module.camera.gb28181.bean.DeviceAlarm;
import com.coalbot.module.core.request.SearchPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName：DeviecAlarmQueryDTO
 * @Author: XinHai.Ma
 * @Date: 2025/10/31 14:26
 * @Description: 设备告警分页查询DTO
 */
@Data
@Schema(description = "设备告警分页查询DTO")
public class DeviceAlarmQueryDTO extends SearchPage<DeviceAlarm> {

    @Schema(description = "设备id")
    private String deviceId;
    @Schema(description = "通道id")
    private String channelId;
    @Schema(description = "查询内容")
    private String alarmPriority;
    @Schema(description = "报警方式")
    private String alarmMethod;
    @Schema(description = "报警级别")
    private String alarmType;
    @Schema(description = "开始时间")
    private String startTime;
    @Schema(description = "结束时间")
    private String endTime;

}
