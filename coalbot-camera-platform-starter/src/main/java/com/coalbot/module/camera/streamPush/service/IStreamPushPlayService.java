package com.coalbot.module.camera.streamPush.service;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.service.bean.ErrorCallback;

public interface IStreamPushPlayService {
    void start(String id, ErrorCallback<StreamInfo> callback, String platformDeviceId, String platformName );

    void stop(String app, String stream);

    void stop(String id);
}
