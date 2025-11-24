package com.coalbot.module.camera.media.service;

import com.coalbot.module.camera.common.CommonCallback;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.gb28181.bean.SendRtpInfo;
import com.coalbot.module.camera.media.bean.MediaInfo;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.bean.RecordInfo;
import com.coalbot.module.camera.service.bean.DownloadFileInfo;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import com.coalbot.module.core.response.RetResult;

import java.util.List;
import java.util.Map;

public interface IMediaNodeServerService {
    int createRTPServer(MediaServer mediaServer, String streamId, long ssrc, Integer port, Boolean onlyAuto, Boolean disableAudio, Boolean reUsePort, Integer tcpMode);

    void closeRtpServer(MediaServer mediaServer, String streamId, CommonCallback<Boolean> callback);


    int createJTTServer(MediaServer mediaServer, String streamId, Integer port, Boolean disableVideo, Boolean disableAudio, Integer tcpMode);

    void closeJTTServer(MediaServer mediaServer, String streamId, CommonCallback<Boolean> callback);


    void closeStreams(MediaServer mediaServer, String app, String stream);

    Boolean updateRtpServerSSRC(MediaServer mediaServer, String stream, String ssrc);

    boolean checkNodeId(MediaServer mediaServer);

    void online(MediaServer mediaServer);

    MediaServer checkMediaServer(String ip, int port, String secret);

    boolean stopSendRtp(MediaServer mediaInfo, String app, String stream, String ssrc);

    boolean initStopSendRtp(MediaServer mediaInfo, String app, String stream, String ssrc);

    boolean deleteRecordDirectory(MediaServer mediaServer, String app, String stream, String date, String fileName);

    List<StreamInfo> getMediaList(MediaServer mediaServer, String app, String stream, String callId);

    Boolean connectRtpServer(MediaServer mediaServer, String address, int port, String stream);

    void getSnap(MediaServer mediaServer, String app, String stream, int timeoutSec, int expireSec, String path, String fileName);

    MediaInfo getMediaInfo(MediaServer mediaServer, String app, String stream);

    Boolean pauseRtpCheck(MediaServer mediaServer, String streamKey);

    Boolean resumeRtpCheck(MediaServer mediaServer, String streamKey);

    String getFfmpegCmd(MediaServer mediaServer, String cmdKey);

    RetResult<String> addStreamProxy(MediaServer mediaServer, String app, String stream, String url, boolean enableAudio, boolean enableMp4, String rtpType, Integer timeout);

    Boolean delFFmpegSource(MediaServer mediaServer, String streamKey);

    Boolean delStreamProxy(MediaServer mediaServer, String streamKey);

    Map<String, String> getFFmpegCMDs(MediaServer mediaServer);

    Integer startSendRtpPassive(MediaServer mediaServer, SendRtpInfo sendRtpItem, Integer timeout);

    void startSendRtpStream(MediaServer mediaServer, SendRtpInfo sendRtpItem);

    Integer startSendRtpTalk(MediaServer mediaServer, SendRtpInfo sendRtpItem, Integer timeout);

    Long updateDownloadProcess(MediaServer mediaServer, String app, String stream);

    String startProxy(MediaServer mediaServer, StreamProxy streamProxy);

    void stopProxy(MediaServer mediaServer, String streamKey, String type);

    List<String> listRtpServer(MediaServer mediaServer);

    void loadMP4FileForDate(MediaServer mediaServer, String app, String stream, String datePath, String dateDir, ErrorCallback<StreamInfo> callback);

    void seekRecordStamp(MediaServer mediaServer, String app, String stream, Double stamp, String schema);

    void setRecordSpeed(MediaServer mediaServer, String app, String stream, Integer speed, String schema);

    DownloadFileInfo getDownloadFilePath(MediaServer mediaServer, RecordInfo recordInfo);

    StreamInfo getStreamInfoByAppAndStream(MediaServer mediaServer, String app, String stream, MediaInfo mediaInfo, String addr, String callId, boolean isPlay);

    void loadMP4File(MediaServer mediaServer, String app, String stream, String filePath, String fileName, ErrorCallback<StreamInfo> callback);
}
