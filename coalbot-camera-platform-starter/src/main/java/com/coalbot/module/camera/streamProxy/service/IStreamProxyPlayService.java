package com.coalbot.module.camera.streamProxy.service;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import javax.validation.constraints.NotNull;


public interface IStreamProxyPlayService {

    void start(String id, Boolean record, ErrorCallback<StreamInfo> callback);

    void startProxy(@NotNull StreamProxy streamProxy, ErrorCallback<StreamInfo> callback);

    void stop(String id);

    void stopProxy(StreamProxy streamProxy);
}
