package com.coalbot.module.camera.gb28181.service;


import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.bean.Device;
import com.coalbot.module.camera.gb28181.bean.Preset;
import com.coalbot.module.camera.service.bean.ErrorCallback;

import java.util.List;

public interface IPTZService {

    void ptz(Device device, String channelId, int cmdCode, int horizonSpeed, int verticalSpeed, int zoomSpeed);

    void frontEndCommand(Device device, String channelId, int cmdCode, int parameter1, int parameter2, int combindCode2);

    void frontEndCommand(CommonGBChannel channel, Integer cmdCode, Integer parameter1, Integer parameter2, Integer combindCode2);

    void queryPresetList(CommonGBChannel channel, ErrorCallback<List<Preset>> callback);

}
