package com.coalbot.module.camera.gb28181.controller.bean;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.gb28181.bean.Device;
import com.coalbot.module.core.response.RetResult;
import org.springframework.web.context.request.async.DeferredResult;

public class PlayResult {

    private DeferredResult<RetResult<StreamInfo>> result;
    private String uuid;

    private Device device;

    public DeferredResult<RetResult<StreamInfo>> getResult() {
        return result;
    }

    public void setResult(DeferredResult<RetResult<StreamInfo>> result) {
        this.result = result;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
