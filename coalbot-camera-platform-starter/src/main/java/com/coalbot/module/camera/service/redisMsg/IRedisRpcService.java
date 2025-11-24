package com.coalbot.module.camera.service.redisMsg;

import com.coalbot.module.camera.common.CommonCallback;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.gb28181.bean.*;
import com.coalbot.module.camera.gb28181.controller.bean.ChannelListForRpcParam;
import com.coalbot.module.camera.gb28181.event.subscribe.catalog.CatalogEvent;
import com.coalbot.module.core.response.RetResult;

import java.util.List;

public interface IRedisRpcService {

    SendRtpInfo getSendRtpItem(String callId);

    RetResult startSendRtp(String callId, SendRtpInfo sendRtpItem);

    RetResult stopSendRtp(String callId);

    long waitePushStreamOnline(SendRtpInfo sendRtpItem, CommonCallback<String> callback);

    void stopWaitePushStreamOnline(SendRtpInfo sendRtpItem);

    void rtpSendStopped(String callId);

    void removeCallback(long key);

    long onStreamOnlineEvent(String app, String stream, CommonCallback<StreamInfo> callback);

    void unPushStreamOnlineEvent(String app, String stream);

    void subscribeCatalog(String id, int cycle);

    void subscribeMobilePosition(String id, int cycle, int interval);

    boolean updatePlatform(String serverId, Platform platform);

    boolean deletePlatform(String serverId, String platformId);

    int addPlatformChannelList(String serverGBId, ChannelListForRpcParam channelListForRpcParam);

    int removeAllPlatformChannel(String serverId, String platformId);

    int removePlatformChannelList(String serverId, ChannelListForRpcParam channelListForRpcParam);

    boolean updateCustomPlatformChannel(String serverId, PlatformChannel channel);

    boolean pushPlatformChannel(String serverId, String platformId);

    void catalogEventPublish(String serverId, CatalogEvent catalogEvent);

    RetResult<SyncStatus> devicesSync(String serverId, String deviceId);

    SyncStatus getChannelSyncStatus(String serverId, String deviceId);

    RetResult<String> deviceBasicConfig(String serverId, Device device, BasicParam basicParam);

    RetResult<String> deviceConfigQuery(String serverId, Device device, String channelId, String configType);

    void teleboot(String serverId, Device device);

    RetResult<String> recordControl(String serverId, Device device, String channelId, String recordCmdStr);

    RetResult<String> guard(String serverId, Device device, String guardCmdStr);

    RetResult<String> resetAlarm(String serverId, Device device, String channelId, String alarmMethod, String alarmType);

    void iFrame(String serverId, Device device, String channelId);

    RetResult<String> homePosition(String serverId, Device device, String channelId, Boolean enabled, Integer resetTime, Integer presetIndex);

    void dragZoomIn(String serverId, Device device, String channelId, int length, int width, int midpointx, int midpointy, int lengthx, int lengthy);

    void dragZoomOut(String serverId, Device device, String channelId, int length, int width, int midpointx, int midpointy, int lengthx, int lengthy);

    RetResult<String> deviceStatus(String serverId, Device device);

    RetResult<String> alarm(String serverId, Device device, String startPriority, String endPriority, String alarmMethod, String alarmType, String startTime, String endTime);

    RetResult<Object> deviceInfo(String serverId, Device device);

    RetResult<List<Preset>> queryPreset(String serverId, Device device, String channelId);
}
