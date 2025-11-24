package com.coalbot.module.camera.service;

import com.coalbot.module.camera.gb28181.bean.SendRtpInfo;
import com.coalbot.module.camera.media.bean.MediaServer;

import java.util.List;

public interface ISendRtpServerService {

    SendRtpInfo createSendRtpInfo(MediaServer mediaServer, String ip, Integer port, String ssrc, String requesterId,
                                  String deviceId, String channelId, Boolean isTcp, Boolean rtcp);

    SendRtpInfo createSendRtpInfo(MediaServer mediaServer, String ip, Integer port, String ssrc, String platformId,
                                  String app, String stream, String channelId, Boolean tcp, Boolean rtcp);

    void update(SendRtpInfo sendRtpItem);

    SendRtpInfo queryByChannelId(String channelId, String targetId);

    SendRtpInfo queryByCallId(String callId);

    List<SendRtpInfo> queryByStream(String stream);

    SendRtpInfo queryByStream(String stream, String targetId);

    void delete(SendRtpInfo sendRtpInfo);

    void deleteByCallId(String callId);

    void deleteByStream(String Stream, String targetId);

    void deleteByChannel(String channelId, String targetId);

    List<SendRtpInfo> queryAll();

    boolean isChannelSendingRTP(String channelId);

    List<SendRtpInfo> queryForPlatform(String platformId);

    List<SendRtpInfo> queryByChannelId(String id);

    void deleteByStream(String stream);

    int getNextPort(MediaServer mediaServer);
}
