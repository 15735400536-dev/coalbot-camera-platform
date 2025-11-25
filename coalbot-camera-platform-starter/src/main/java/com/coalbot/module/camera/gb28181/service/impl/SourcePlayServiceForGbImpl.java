package com.coalbot.module.camera.gb28181.service.impl;

import com.coalbot.module.camera.common.InviteSessionType;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.common.enums.ChannelDataType;

import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.bean.Platform;
import com.coalbot.module.camera.gb28181.bean.PlayException;
import com.coalbot.module.camera.gb28181.service.IPlayService;
import com.coalbot.module.camera.gb28181.service.ISourcePlayService;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.core.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sip.message.Response;

@Slf4j
@Service(ChannelDataType.PLAY_SERVICE + ChannelDataType.GB28181)
public class SourcePlayServiceForGbImpl implements ISourcePlayService {

    @Autowired
    private IPlayService deviceChannelPlayService;

    @Override
    public void play(CommonGBChannel channel, Platform platform, Boolean record, ErrorCallback<StreamInfo> callback) {
        // 国标通道
        try {
            deviceChannelPlayService.play(channel, record, callback);
        } catch (PlayException e) {
            callback.run(e.getCode(), e.getMessage(), null);
        } catch (CommonException e) {
            log.error("[点播失败] {}({}), {}", channel.getGbName(), channel.getGbDeviceId(), e.getMessage());
            callback.run(Response.BUSY_HERE, "busy here", null);
        } catch (Exception e) {
            log.error("[点播失败] {}({})", channel.getGbName(), channel.getGbDeviceId(), e);
            callback.run(Response.BUSY_HERE, "busy here", null);
        }
    }

    @Override
    public void stopPlay(CommonGBChannel channel) {
        // 国标通道
        try {
            deviceChannelPlayService.stopPlay(InviteSessionType.PLAY, channel);
        }  catch (Exception e) {
            log.error("[停止点播失败] {}({})", channel.getGbName(), channel.getGbDeviceId(), e);
        }
    }
}
