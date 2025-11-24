package com.coalbot.module.camera.jt1078.bean;

import com.coalbot.module.camera.common.enums.ChannelDataType;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * JT 通道
 */
@Data
@Schema(description = "jt808通道")
@EqualsAndHashCode(callSuper = true)
public class JTChannel extends CommonGBChannel {

    @Schema(description = "数据库自增ID")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "设备的数据库ID")
    private String terminalDbId;

    @Schema(description = "通道ID")
    private String channelId;

    @Schema(description = "是否含有音频")
    private boolean hasAudio;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Schema(description = "流信息")
    private String stream;

    private Integer dataType = ChannelDataType.JT_1078;

    @Override
    public String toString() {
        return "JTChannel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", terminalDbId=" + terminalDbId +
                ", channelId=" + channelId +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", hasAudio='" + hasAudio + '\'' +
                '}';
    }

    public CommonGBChannel buildCommonGBChannel() {
        if (ObjectUtils.isEmpty(this.getGbDeviceId())) {
            return null;
        }
        if (ObjectUtils.isEmpty(this.getGbName())) {
            this.setGbName(this.getName());
        }
        return this;

    }
}
