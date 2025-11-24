package com.coalbot.module.camera.jt1078.service;

import com.coalbot.module.camera.common.CommonCallback;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.jt1078.bean.JTMediaStreamType;
import com.coalbot.module.camera.jt1078.proc.request.J1205;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.core.response.RetResult;

import java.util.List;

public interface Ijt1078PlayService {

    JTMediaStreamType checkStreamFromJt(String stream);

    void play(String phoneNumber, String channelId, int type, CommonCallback<RetResult<StreamInfo>> callback);

    void playback(String phoneNumber, String channelId, String startTime, String endTime, Integer type,
                  Integer rate, Integer playbackType, Integer playbackSpeed, CommonCallback<RetResult<StreamInfo>> callback);

    void stopPlay(String phoneNumber, String channelId);

    void pausePlay(String phoneNumber, String channelId);

    void continueLivePlay(String phoneNumber, String channelId);

    List<J1205.JRecordItem> getRecordList(String phoneNumber, String channelId, String startTime, String endTime);

    void stopPlayback(String phoneNumber, String channelId);

    StreamInfo startTalk(String phoneNumber, String channelId);

    void stopTalk(String phoneNumber, String channelId);

    void playbackControl(String phoneNumber, String channelId, Integer command, Integer playbackSpeed, String time);

    void start(String channelId, Boolean record, ErrorCallback<StreamInfo> callback);

    void stop(String channelId);

    void playBack(String channelId, Long startTime, Long stopTime, ErrorCallback<StreamInfo> callback);
}
