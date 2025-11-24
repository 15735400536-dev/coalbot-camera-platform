package com.coalbot.module.camera.service.redisMsg;

import com.coalbot.module.camera.common.InviteSessionType;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.gb28181.bean.RecordInfo;
import com.coalbot.module.camera.service.bean.DownloadFileInfo;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.vmanager.bean.AudioBroadcastResult;

public interface IRedisRpcPlayService {


    void play(String serverId, String channelId, ErrorCallback<StreamInfo> callback);

    void stop(String serverId, InviteSessionType type, String channelId, String stream);

    void playback(String serverId, String channelId, String startTime, String endTime, ErrorCallback<StreamInfo> callback);

    void playbackPause(String serverId, String streamId);

    void playbackResume(String serverId, String streamId);

    void download(String serverId, String channelId, String startTime, String endTime, int downloadSpeed, ErrorCallback<StreamInfo> callback);

    void queryRecordInfo(String serverId, String channelId, String startTime, String endTime, ErrorCallback<RecordInfo> callback);

    String frontEndCommand(String serverId, String channelId, int cmdCode, int parameter1, int parameter2, int combindCode2);

    void playPush(String serverId, String id, ErrorCallback<StreamInfo> callback);

    void playProxy(String serverId, String id, ErrorCallback<StreamInfo> callback);

    void stopProxy(String serverId, String id);

    DownloadFileInfo getRecordPlayUrl(String serverId, String recordId);

    AudioBroadcastResult audioBroadcast(String serverId, String deviceId, String channelDeviceId, Boolean broadcastMode);
}
