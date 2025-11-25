package com.coalbot.module.camera.service.impl;

import com.coalbot.module.camera.common.InviteInfo;
import com.coalbot.module.camera.common.InviteSessionStatus;
import com.coalbot.module.camera.common.InviteSessionType;
import com.coalbot.module.camera.common.VideoManagerConstants;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.gb28181.bean.DeviceChannel;
import com.coalbot.module.camera.gb28181.bean.SsrcTransaction;
import com.coalbot.module.camera.gb28181.service.IDeviceChannelService;
import com.coalbot.module.camera.gb28181.service.IInviteStreamService;
import com.coalbot.module.camera.gb28181.session.SipInviteSessionManager;
import com.coalbot.module.camera.jt1078.bean.JTMediaStreamType;
import com.coalbot.module.camera.jt1078.service.Ijt1078PlayService;
import com.coalbot.module.camera.jt1078.service.Ijt1078Service;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.bean.ResultForOnPublish;
import com.coalbot.module.camera.media.zlm.dto.StreamAuthorityInfo;
import com.coalbot.module.camera.service.IMediaService;
import com.coalbot.module.camera.service.IRecordPlanService;
import com.coalbot.module.camera.service.ISendRtpServerService;
import com.coalbot.module.camera.storager.IRedisCatchStorage;
import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyService;
import com.coalbot.module.camera.utils.DateUtil;
import com.coalbot.module.camera.utils.MediaServerUtils;
import com.coalbot.module.camera.vmanager.bean.OtherPsSendInfo;
import com.coalbot.module.camera.vmanager.bean.OtherRtpSendInfo;
import com.coalbot.module.core.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class MediaServiceImpl implements IMediaService {

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private IStreamProxyService streamProxyService;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

//    @Autowired
//    private IUserService userService;

    @Autowired
    private IInviteStreamService inviteStreamService;

    @Autowired
    private IDeviceChannelService deviceChannelService;

    @Autowired
    private SipInviteSessionManager sessionManager;

    @Autowired
    private Ijt1078Service ijt1078Service;

    @Autowired
    private Ijt1078PlayService jt1078PlayService;

    @Autowired
    private ISendRtpServerService sendRtpServerService;


    @Autowired
    private IRecordPlanService recordPlanService;

    @Override
    public boolean authenticatePlay(String app, String stream, String callId) {
        if (app == null || stream == null) {
            return false;
        }
        if ("rtp".equals(app)) {
            return true;
        }
        StreamAuthorityInfo streamAuthorityInfo = redisCatchStorage.getStreamAuthorityInfo(app, stream);
        if (streamAuthorityInfo == null || streamAuthorityInfo.getCallId() == null) {
            return true;
        }
        return streamAuthorityInfo.getCallId().equals(callId);
    }

    @Override
    public ResultForOnPublish authenticatePublish(MediaServer mediaServer, String app, String stream, String params) {
        // 推流鉴权的处理
        if (!"rtp".equals(app) && !"1078".equals(app) ) {
            if ("talk".equals(app) && stream.endsWith("_talk")) {
                ResultForOnPublish result = new ResultForOnPublish();
                result.setEnable_mp4(false);
                result.setEnable_audio(true);
                return result;
            }
            if ("jt_talk".equals(app) && stream.endsWith("_talk")) {
                ResultForOnPublish result = new ResultForOnPublish();
                result.setEnable_mp4(false);
                result.setEnable_audio(true);
                return result;
            }
            if ("mp4_record".equals(app) ) {
                ResultForOnPublish result = new ResultForOnPublish();
                result.setEnable_mp4(false);
                result.setEnable_audio(true);
                return result;
            }
            StreamProxy streamProxyItem = streamProxyService.getStreamProxyByAppAndStream(app, stream);
            if (streamProxyItem != null) {
                ResultForOnPublish result = new ResultForOnPublish();
                result.setEnable_audio(streamProxyItem.isEnableAudio());
                result.setEnable_mp4(streamProxyItem.isEnableMp4());
                return result;
            }
            if (userSetting.getPushAuthority()) {
                // 对于推流进行鉴权
                Map<String, String> paramMap = MediaServerUtils.urlParamToMap(params);
                // 推流鉴权
                if (params == null) {
                    log.info("推流鉴权失败： 缺少必要参数：sign=md5(user表的pushKey)");
                    throw new CommonException("Unauthorized");
                }

                String sign = paramMap.get("sign");
                if (sign == null) {
                    log.info("推流鉴权失败： 缺少必要参数：sign=md5(user表的pushKey)");
                    throw new CommonException("Unauthorized");
                }
                // 推流自定义播放鉴权码
                String callId = paramMap.get("callId");
                // 鉴权配置
//                boolean hasAuthority = userService.checkPushAuthority(callId, sign);
//                if (!hasAuthority) {
//                    log.info("推流鉴权失败： sign 无权限: callId={}. sign={}", callId, sign);
//                    throw new CommonException("Unauthorized");
//                }
                StreamAuthorityInfo streamAuthorityInfo = StreamAuthorityInfo.getInstanceByHook(app, stream, mediaServer.getId());
                streamAuthorityInfo.setCallId(callId);
                streamAuthorityInfo.setSign(sign);
                // 鉴权通过
                redisCatchStorage.updateStreamAuthorityInfo(app, stream, streamAuthorityInfo);
            }
        }


        ResultForOnPublish result = new ResultForOnPublish();
        result.setEnable_audio(true);

        // 国标流
        if ("rtp".equals(app)) {

            InviteInfo inviteInfo = inviteStreamService.getInviteInfoByStream(null, stream);

            if (inviteInfo != null) {
                result.setEnable_mp4(inviteInfo.getRecord());
            }else {
                result.setEnable_mp4(userSetting.getRecordSip());
            }

            // 单端口模式下修改流 ID
            if (!mediaServer.isRtpEnable() && inviteInfo == null) {
                String ssrc = String.format("%010d", Long.parseLong(stream, 16));
                inviteInfo = inviteStreamService.getInviteInfoBySSRC(ssrc);
                if (inviteInfo != null) {
                    result.setStream_replace(inviteInfo.getStream());
                    log.info("[HOOK]推流鉴权 stream: {} 替换为 {}", stream, inviteInfo.getStream());
                    stream = inviteInfo.getStream();
                }
            }

            // 设置音频信息及录制信息
            SsrcTransaction ssrcTransaction = sessionManager.getSsrcTransactionByStream(app, stream);
            if (ssrcTransaction != null ) {

                // 为录制国标模拟一个鉴权信息, 方便后续写入录像文件时使用
                StreamAuthorityInfo streamAuthorityInfo = StreamAuthorityInfo.getInstanceByHook(app, stream, mediaServer.getId());
                streamAuthorityInfo.setApp(app);
                streamAuthorityInfo.setStream(ssrcTransaction.getStream());
                streamAuthorityInfo.setCallId(ssrcTransaction.getSipTransactionInfo().getCallId());

                redisCatchStorage.updateStreamAuthorityInfo(app, ssrcTransaction.getStream(), streamAuthorityInfo);

                String deviceId = ssrcTransaction.getDeviceId();
                String channelId = ssrcTransaction.getChannelId();
                DeviceChannel deviceChannel = deviceChannelService.getOneForSourceById(channelId);
                if (deviceChannel != null) {
                    result.setEnable_audio(deviceChannel.isHasAudio());
                }
                // 如果是录像下载就设置视频间隔十秒
                if (ssrcTransaction.getType() == InviteSessionType.DOWNLOAD) {
                    // 获取录像的总时长，然后设置为这个视频的时长
                    InviteInfo inviteInfoForDownload = inviteStreamService.getInviteInfo(InviteSessionType.DOWNLOAD,  channelId, stream);
                    if (inviteInfoForDownload != null) {
                        String startTime = inviteInfoForDownload.getStartTime();
                        String endTime = inviteInfoForDownload.getEndTime();
                        long difference = DateUtil.getDifference(startTime, endTime) / 1000;
                        result.setMp4_max_second((int) difference);
                        result.setEnable_mp4(true);
                        // 设置为2保证得到的mp4的时长是正常的
                        result.setModify_stamp(2);
                    }
                }
                // 如果是talk对讲，则默认获取声音
                if (ssrcTransaction.getType() == InviteSessionType.TALK) {
                    result.setEnable_audio(true);
                }
            }
        } else if (app.equals("broadcast")) {
            result.setEnable_audio(true);
            result.setEnable_mp4(userSetting.getRecordSip());
        } else if (app.equals("talk")) {
            result.setEnable_audio(true);
            result.setEnable_mp4(userSetting.getRecordSip());
        }else {
            result.setEnable_mp4(userSetting.getRecordPushLive());
        }
        if (app.equalsIgnoreCase("rtp")) {
            String receiveKey = VideoManagerConstants.WVP_OTHER_RECEIVE_RTP_INFO + userSetting.getServerId() + "_" + stream;
            OtherRtpSendInfo otherRtpSendInfo = (OtherRtpSendInfo) redisTemplate.opsForValue().get(receiveKey);

            String receiveKeyForPS = VideoManagerConstants.WVP_OTHER_RECEIVE_PS_INFO + userSetting.getServerId() + "_" + stream;
            OtherPsSendInfo otherPsSendInfo = (OtherPsSendInfo) redisTemplate.opsForValue().get(receiveKeyForPS);
            if (otherRtpSendInfo != null || otherPsSendInfo != null) {
                result.setEnable_mp4(true);
            }
        }
        return result;
    }

    @Override
    public boolean closeStreamOnNoneReader(String mediaServerId, String app, String stream, String schema) {
        boolean result = false;
        if (recordPlanService.recording(app, stream) != null) {
            return false;
        }
        // 国标类型的流
        if ("rtp".equals(app)) {
            result = userSetting.getStreamOnDemand();
            // 国标流， 点播/录像回放/录像下载
            InviteInfo inviteInfo = inviteStreamService.getInviteInfoByStream(null, stream);
            if (inviteInfo != null) {
                if (inviteInfo.getStatus() == InviteSessionStatus.ok){
                    // 录像下载
                    if (inviteInfo.getType() == InviteSessionType.DOWNLOAD) {
                        return false;
                    }
                    DeviceChannel deviceChannel = deviceChannelService.getOneForSourceById(inviteInfo.getChannelId());
                    if (deviceChannel == null) {
                        return false;
                    }
                }
                return result;
            }
        }else if ("1078".equals(app)) {
            // 判断是否是1078播放类型
            JTMediaStreamType jtMediaStreamType = ijt1078Service.checkStreamFromJt(stream);
            if (jtMediaStreamType != null) {
                String[] streamParamArray = stream.split("_");
                if (jtMediaStreamType.equals(JTMediaStreamType.PLAY)) {
                    jt1078PlayService.stopPlay(streamParamArray[0], streamParamArray[1]);
                }else if (jtMediaStreamType.equals(JTMediaStreamType.PLAYBACK)) {
                    jt1078PlayService.stopPlayback(streamParamArray[0], streamParamArray[1]);
                }
            }else {
                return false;
            }
        }else if ("talk".equals(app) || "broadcast".equals(app)) {
            return false;
        } else if ("mp4_record".equals(app)) {
            return true;
        } else {
            // 非国标流 推流/拉流代理
            // 拉流代理
            StreamProxy streamProxy = streamProxyService.getStreamProxyByAppAndStream(app, stream);
            if (streamProxy != null) {
                 if (streamProxy.isEnableDisableNoneReader()) {
                    // 无人观看停用
                    // 修改数据
                    streamProxyService.stopByAppAndStream(app, stream);
                    return true;
                } else {
                    // 无人观看不做处理
                    return false;
                }
            }else {
                return false;
            }
        }
        return result;
    }
}
