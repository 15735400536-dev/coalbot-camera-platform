package com.coalbot.module.camera.service.redisMsg.control;

import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcRequest;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcResponse;
import com.coalbot.module.camera.gb28181.bean.SendRtpInfo;
import com.coalbot.module.camera.gb28181.session.SSRCFactory;
import com.coalbot.module.camera.media.bean.MediaInfo;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.service.ISendRtpServerService;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcController;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcMapping;
import com.coalbot.module.camera.service.redisMsg.dto.RpcController;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.RetCode;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RedisRpcController("sendRtp")
public class RedisRpcSendRtpController extends RpcController {

    @Autowired
    private SSRCFactory ssrcFactory;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private ISendRtpServerService sendRtpServerService;

    @Autowired
    private UserSetting userSetting;


    /**
     * 获取发流的信息
     */
    @RedisRpcMapping("getSendRtpItem")
    public RedisRpcResponse getSendRtpItem(RedisRpcRequest request) {
        String callId = request.getParam().toString();
        SendRtpInfo sendRtpItem = sendRtpServerService.queryByCallId(callId);
        if (sendRtpItem == null) {
            log.info("[redis-rpc] 获取发流的信息, 未找到redis中的发流信息， callId：{}", callId);
            RedisRpcResponse response = request.getResponse();
            response.setStatusCode(ErrorCode.SUCCESS.getCode());
            return response;
        }
        log.info("[redis-rpc] 获取发流的信息： {}/{}, 目标地址： {}：{}", sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort());
        // 查询本级是否有这个流
        MediaServer mediaServerItem = mediaServerService.getMediaServerByAppAndStream(sendRtpItem.getApp(), sendRtpItem.getStream());
        if (mediaServerItem == null) {
            RedisRpcResponse response = request.getResponse();
            response.setStatusCode(ErrorCode.SUCCESS.getCode());
        }
        // 自平台内容
        int localPort = sendRtpServerService.getNextPort(mediaServerItem);
        if (localPort <= 0) {
            log.info("[redis-rpc] getSendRtpItem->服务器端口资源不足" );
            RedisRpcResponse response = request.getResponse();
            response.setStatusCode(ErrorCode.SUCCESS.getCode());
        }
        // 写入redis， 超时时回复
        sendRtpItem.setStatus(1);
        sendRtpItem.setServerId(userSetting.getServerId());
        sendRtpItem.setLocalIp(mediaServerItem.getSdpIp());
        if (sendRtpItem.getSsrc() == null) {
            // 上级平台点播时不使用上级平台指定的ssrc，使用自定义的ssrc，参考国标文档-点播外域设备媒体流SSRC处理方式
            String ssrc = "Play".equalsIgnoreCase(sendRtpItem.getSessionName()) ? ssrcFactory.getPlaySsrc(mediaServerItem.getId()) : ssrcFactory.getPlayBackSsrc(mediaServerItem.getId());
            sendRtpItem.setSsrc(ssrc);
        }
        sendRtpServerService.update(sendRtpItem);
        RedisRpcResponse response = request.getResponse();
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        response.setBody(callId);
        return response;
    }

    /**
     * 开始发流
     */
    @RedisRpcMapping("startSendRtp")
    public RedisRpcResponse startSendRtp(RedisRpcRequest request) {
        String callId = request.getParam().toString();
        SendRtpInfo sendRtpItem = sendRtpServerService.queryByCallId(callId);
        RedisRpcResponse response = request.getResponse();
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        if (sendRtpItem == null) {
            log.info("[redis-rpc] 开始发流, 未找到redis中的发流信息， callId：{}", callId);
            RetResult RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "未找到redis中的发流信息");
            response.setBody(RetResult);
            return response;
        }
        log.info("[redis-rpc] 开始发流： {}/{}, 目标地址： {}：{}", sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort());
        MediaServer mediaServer = mediaServerService.getOne(sendRtpItem.getMediaServerId());
        if (mediaServer == null) {
            log.info("[redis-rpc] startSendRtp->未找到MediaServer： {}", sendRtpItem.getMediaServerId() );
            RetResult RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "未找到MediaServer");
            response.setBody(RetResult);
            return response;
        }
        MediaInfo mediaInfo = mediaServerService.getMediaInfo(mediaServer, sendRtpItem.getApp(), sendRtpItem.getStream());
        if (mediaInfo == null) {
            log.info("[redis-rpc] startSendRtp->流不在线： {}/{}", sendRtpItem.getApp(), sendRtpItem.getStream() );
            RetResult RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "流不在线");
            response.setBody(RetResult);
            return response;
        }
        try {
            mediaServerService.startSendRtp(mediaServer, sendRtpItem);
        }catch (CommonException exception) {
            log.info("[redis-rpc] 发流失败： {}/{}, 目标地址： {}：{}， {}", sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort(), exception.getMessage());
            RetResult RetResult = RetResponse.makeRsp(RetCode.INTERNAL_SERVER_ERROR.code, exception.getMessage());
            response.setBody(RetResult);
            return response;
        }
        log.info("[redis-rpc] 发流成功： {}/{}, 目标地址： {}：{}", sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort());
        RetResult RetResult = RetResponse.makeOKRsp();
        response.setBody(RetResult);
        return response;
    }

    /**
     * 停止发流
     */
    @RedisRpcMapping("stopSendRtp")
    public RedisRpcResponse stopSendRtp(RedisRpcRequest request) {
        String callId = request.getParam().toString();
        SendRtpInfo sendRtpItem = sendRtpServerService.queryByCallId(callId);
        RedisRpcResponse response = request.getResponse();
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        if (sendRtpItem == null) {
            log.info("[redis-rpc] 停止推流, 未找到redis中的发流信息， key：{}", callId);
            RetResult RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "未找到redis中的发流信息");
            response.setBody(RetResult);
            return response;
        }
        log.info("[redis-rpc] 停止推流： {}/{}, 目标地址： {}：{}", sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort() );
        MediaServer mediaServer = mediaServerService.getOne(sendRtpItem.getMediaServerId());
        if (mediaServer == null) {
            log.info("[redis-rpc] stopSendRtp->未找到MediaServer： {}", sendRtpItem.getMediaServerId() );
            RetResult RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "未找到MediaServer");
            response.setBody(RetResult);
            return response;
        }
        try {
            mediaServerService.stopSendRtp(mediaServer, sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getSsrc());
        }catch (CommonException exception) {
            log.info("[redis-rpc] 停止推流失败： {}/{}, 目标地址： {}：{}， code： {}, msg: {}", sendRtpItem.getApp(),
                    sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort(), RetCode.INTERNAL_SERVER_ERROR.code, exception.getMessage() );
            response.setBody(RetResponse.makeRsp(RetCode.INTERNAL_SERVER_ERROR.code, exception.getMessage()));
            return response;
        }
        log.info("[redis-rpc] 停止推流成功： {}/{}, 目标地址： {}：{}", sendRtpItem.getApp(), sendRtpItem.getStream(), sendRtpItem.getIp(), sendRtpItem.getPort() );
        response.setBody(RetResponse.makeOKRsp());
        return response;
    }

}
