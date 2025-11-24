package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.common.enums.DeviceControlType;
import com.coalbot.module.camera.gb28181.bean.*;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.vmanager.bean.ResourceBaseInfo;
import com.coalbot.module.camera.web.gb28181.dto.DeviceChannelExtend;
import com.github.pagehelper.PageInfo;
import org.dom4j.Element;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 国标通道业务类
 * @author lin
 */
public interface IDeviceChannelService {

    /**
     * 批量添加设备通道
     */
    int updateChannels(Device device, List<DeviceChannel> channels);

    /**
     * 获取统计信息
     * @return
     */
    ResourceBaseInfo getOverview();

    /**
     *  获取一个通道
     */
    DeviceChannel getOne(String deviceId, String channelId);

    DeviceChannel getOneForSource(String deviceId, String channelId);

    /**
     * 修改通道的码流类型
     */
    void updateChannelStreamIdentification(DeviceChannel channel);

    List<DeviceChannel> queryChaneListByDeviceId(String deviceId);

    void updateChannelGPS(Device device, DeviceChannel deviceChannel, MobilePosition mobilePosition);

    void startPlay(String channelId, String stream);

    void stopPlay(String channelId);

    void online(DeviceChannel channel);

    void offline(DeviceChannel channel);

    void deleteForNotify(DeviceChannel channel);

    void cleanChannelsForDevice(String deviceId);

    boolean resetChannels(String deviceDbId, List<DeviceChannel> deviceChannels);

    PageInfo<DeviceChannel> getSubChannels(String deviceDbId, String channelId, String query, Boolean channelType, Boolean online, int page, int count);

    List<DeviceChannelExtend> queryChannelExtendsByDeviceId(String deviceId, List<String> channelIds, Boolean online);

    PageInfo<DeviceChannel> queryChannelsByDeviceId(String deviceId, String query, Boolean channelType, Boolean online, int page, int count);

    PageInfo<DeviceChannel> queryChannels(String query, Boolean queryParent, Boolean channelType, Boolean online, Boolean hasStream, int page, int count);

    List<Device> queryDeviceWithAsMessageChannel();

    DeviceChannel getRawChannel(String id);

    DeviceChannel getOneById(String channelId);

    DeviceChannel getOneForSourceById(String channelId);

    DeviceChannel getBroadcastChannel(String deviceDbId);

    void changeAudio(String channelId, Boolean audio);

    void updateChannelStatusForNotify(DeviceChannel channel);

    void addChannel(DeviceChannel channel);

    void updateChannelForNotify(DeviceChannel channel);

    DeviceChannel getOneForSourceEx(String deviceDbId, String channelId);

    DeviceChannel getOneBySourceId(String deviceDbId, String channelId);

    List<String> queryChaneIdListByDeviceDbIds(List<String> deviceDbId);

    void handlePtzCmd(@NotNull String dataDeviceId, @NotNull String gbId, Element rootElement, DeviceControlType type, ErrorCallback<String> callback);

    void queryRecordInfo(Device device, DeviceChannel channel, String startTime, String endTime, ErrorCallback<RecordInfo> object);

    void queryRecordInfo(CommonGBChannel channel, String startTime, String endTime, ErrorCallback<RecordInfo> object);

}
