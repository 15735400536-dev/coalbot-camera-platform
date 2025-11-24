package com.coalbot.module.camera.service.redisMsg.dto;


import com.coalbot.module.camera.conf.redis.RedisRpcConfig;
import com.coalbot.module.camera.conf.redis.bean.RedisRpcClassHandler;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

public class RpcController {

    @Autowired
    private RedisRpcConfig redisRpcConfig;


    @PostConstruct
    public RetResult<Void> init() {
        String controllerPath = this.getClass().getAnnotation(RedisRpcController.class).value();
        // 扫描其下的方法
        Method[] methods = this.getClass().getDeclaredMethods();
        for (Method method : methods) {
            RedisRpcMapping annotation = method.getAnnotation(RedisRpcMapping.class);
            if (annotation != null) {
                String methodPath = annotation.value();
                if (methodPath != null) {
                    redisRpcConfig.addHandler(controllerPath + "/" + methodPath, new RedisRpcClassHandler(this, method));
                }
            }
        }
        return RetResponse.makeOKRsp();
    }
}
