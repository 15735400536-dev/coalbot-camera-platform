package com.coalbot.module.camera.service.redisMsg.control;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.redis.RedisRpcConfig;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcMessage;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcRequest;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcResponse;
import com.coalbot.module.camera.gb28181.bean.Device;
import com.coalbot.module.camera.gb28181.service.IDeviceService;
import com.coalbot.module.camera.gb28181.service.IPlayService;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcController;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcMapping;
import com.coalbot.module.camera.service.redisMsg.dto.RpcController;
import com.coalbot.module.camera.vmanager.bean.AudioBroadcastResult;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RedisRpcController("devicePlay")
public class RedisRpcDevicePlayController extends RpcController {

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IPlayService playService;



    private RetResult<Void> sendResponse(RedisRpcResponse response){
        log.info("[redis-rpc] >> {}", response);
        response.setToId(userSetting.getServerId());
        RedisRpcMessage message = new RedisRpcMessage();
        message.setResponse(response);
        redisTemplate.convertAndSend(RedisRpcConfig.REDIS_REQUEST_CHANNEL_KEY, message);
        return RetResponse.makeOKRsp();
    }

    /**
     * 获取通道同步状态
     */
    @RedisRpcMapping("audioBroadcast")
    public RedisRpcResponse audioBroadcast(RedisRpcRequest request) {
        JSONObject paramJson = JSON.parseObject(request.getParam().toString());
        String deviceId = paramJson.getString("deviceId");
        String channelDeviceId = paramJson.getString("channelDeviceId");
        Boolean broadcastMode = paramJson.getBoolean("broadcastMode");

        Device device = deviceService.getDeviceByDeviceId(deviceId);

        RedisRpcResponse response = request.getResponse();
        if (device == null || !userSetting.getServerId().equals(device.getServerId())) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        AudioBroadcastResult audioBroadcastResult = playService.audioBroadcast(deviceId, channelDeviceId, broadcastMode);
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        response.setBody(JSONObject.toJSONString(audioBroadcastResult));
        return response;
    }

}
