package com.coalbot.module.camera.gb28181.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * 业务分组
 */
@Data
@Schema(description = "业务分组")
public class Group implements Comparable<Group>{
    /**
     * 数据库自增ID
     */
    @Schema(description = "数据库自增ID")
    private String id;

    /**
     * 区域国标编号
     */
    @Schema(description = "区域国标编号")
    private String deviceId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String name;

    /**
     * 父分组ID
     */
    @Schema(description = "父分组ID")
    private String parentId;

    /**
     * 父区域国标ID
     */
    @Schema(description = "父区域国标ID")
    private String parentDeviceId;

    /**
     * 所属的业务分组国标编号
     */
    @Schema(description = "所属的业务分组国标编号")
    private String businessGroup;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 行政区划
     */
    @Schema(description = "行政区划")
    private String civilCode;

    /**
     * 别名
     */
    @Schema(description = "别名， 此别名为唯一值，可以对接第三方是存储对方的ID")
    private String alias;

    public static Group getInstance(DeviceChannel channel) {
        GbCode gbCode = GbCode.decode(channel.getDeviceId());
        if (gbCode == null || (!gbCode.getTypeCode().equals("215") && !gbCode.getTypeCode().equals("216"))) {
            return null;
        }
        Group group = new Group();
        group.setName(channel.getName());
        group.setDeviceId(channel.getDeviceId());
        group.setCreateTime(new Date());
        group.setUpdateTime(new Date());
        if (gbCode.getTypeCode().equals("215")) {
            group.setBusinessGroup(channel.getDeviceId());
        }else if (gbCode.getTypeCode().equals("216")) {
            group.setBusinessGroup(channel.getBusinessGroupId());
            group.setParentDeviceId(channel.getParentId());
        }
        if (group.getBusinessGroup() == null) {
            return null;
        }
        return group;
    }

    @Override
    public int compareTo(@NotNull Group region) {
        return Integer.compare(Integer.parseInt(this.deviceId), Integer.parseInt(region.getDeviceId()));
    }
}
