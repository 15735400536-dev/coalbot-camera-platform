package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.bean.Platform;
import com.coalbot.module.camera.service.bean.ErrorCallback;

/**
 * 资源能力接入-实时录像
 */
public interface ISourcePlayService {

    void play(CommonGBChannel channel, Platform platform, Boolean record, ErrorCallback<StreamInfo> callback);

    void stopPlay(CommonGBChannel channel);

}
