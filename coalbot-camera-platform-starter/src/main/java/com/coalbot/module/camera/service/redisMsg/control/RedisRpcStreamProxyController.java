package com.coalbot.module.camera.service.redisMsg.control;

import com.alibaba.fastjson2.JSONObject;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.redis.RedisRpcConfig;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcMessage;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcRequest;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcResponse;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcController;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcMapping;
import com.coalbot.module.camera.service.redisMsg.dto.RpcController;
import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyPlayService;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyService;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RedisRpcController("streamProxy")
public class RedisRpcStreamProxyController extends RpcController {

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private IStreamProxyPlayService streamProxyPlayService;

    @Autowired
    private IStreamProxyService streamProxyService;


    private RetResult<Void> sendResponse(RedisRpcResponse response){
        log.info("[redis-rpc] >> {}", response);
        response.setToId(userSetting.getServerId());
        RedisRpcMessage message = new RedisRpcMessage();
        message.setResponse(response);
        redisTemplate.convertAndSend(RedisRpcConfig.REDIS_REQUEST_CHANNEL_KEY, message);
        return RetResponse.makeOKRsp();
    }

    /**
     * 播放
     */
    @RedisRpcMapping("play")
    public RedisRpcResponse play(RedisRpcRequest request) {
        String id = request.getParam().toString();
        RedisRpcResponse response = request.getResponse();
        if (Objects.isNull(id)) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        StreamProxy streamProxy = streamProxyService.getStreamProxy(id);
        if (streamProxy == null) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        streamProxyPlayService.startProxy(streamProxy, (code, msg, streamInfo) -> {
            response.setStatusCode(code);
            response.setBody(JSONObject.toJSONString(streamInfo));
            sendResponse(response);
        });

        return null;
    }

    /**
     * 停止
     */
    @RedisRpcMapping("stop")
    public RedisRpcResponse stop(RedisRpcRequest request) {
        String id = request.getParam().toString();
        RedisRpcResponse response = request.getResponse();
        if (Objects.isNull(id)) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        StreamProxy streamProxy = streamProxyService.getStreamProxy(id);
        if (streamProxy == null) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        streamProxyPlayService.stopProxy(streamProxy);
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

}
