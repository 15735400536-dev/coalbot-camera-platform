package com.coalbot.module.camera.service.redisMsg.control;

import com.alibaba.fastjson2.JSONObject;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.redis.RedisRpcConfig;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcMessage;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcRequest;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcResponse;
import com.coalbot.module.camera.gb28181.service.IDeviceService;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcController;
import com.coalbot.module.camera.service.redisMsg.dto.RedisRpcMapping;
import com.coalbot.module.camera.service.redisMsg.dto.RpcController;
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
@RedisRpcController("device")
public class RedisRpcGbDeviceController extends RpcController {

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private IDeviceService deviceService;



    private RetResult<Void> sendResponse(RedisRpcResponse response){
        log.info("[redis-rpc] >> {}", response);
        response.setToId(userSetting.getServerId());
        RedisRpcMessage message = new RedisRpcMessage();
        message.setResponse(response);
        redisTemplate.convertAndSend(RedisRpcConfig.REDIS_REQUEST_CHANNEL_KEY, message);
        return RetResponse.makeOKRsp();
    }


    /**
     * 目录订阅
     */
    @RedisRpcMapping("subscribeCatalog")
    public RedisRpcResponse subscribeCatalog(RedisRpcRequest request) {
        JSONObject paramJson = JSONObject.parseObject(request.getParam().toString());
        String id = paramJson.getString("id");
        int cycle = paramJson.getIntValue("cycle");

        RedisRpcResponse response = request.getResponse();

        if (Objects.isNull(id)) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        deviceService.subscribeCatalog(id, cycle);
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

    /**
     * 移动位置订阅
     */
    @RedisRpcMapping("subscribeMobilePosition")
    public RedisRpcResponse subscribeMobilePosition(RedisRpcRequest request) {
        JSONObject paramJson = JSONObject.parseObject(request.getParam().toString());
        String id = paramJson.getString("id");
        int cycle = paramJson.getIntValue("cycle");
        int interval = paramJson.getIntValue("interval");

        RedisRpcResponse response = request.getResponse();

        if (Objects.isNull(id)) {
            response.setStatusCode(ErrorCode.ERROR400.getCode());
            response.setBody("param error");
            return response;
        }
        deviceService.subscribeMobilePosition(id, cycle, interval);
        response.setStatusCode(ErrorCode.SUCCESS.getCode());
        return response;
    }

}
