package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.gb28181.bean.*;
import com.coalbot.module.camera.service.bean.ErrorCallback;

import java.util.List;

/**
 * 资源能力接入-云台控制
 */
public interface ISourcePTZService {

    void ptz(CommonGBChannel channel, FrontEndControlCodeForPTZ frontEndControlCode, ErrorCallback<String> callback);

    void preset(CommonGBChannel channel, FrontEndControlCodeForPreset frontEndControlCode, ErrorCallback<String> callback);

    void fi(CommonGBChannel channel, FrontEndControlCodeForFI frontEndControlCode, ErrorCallback<String> callback);

    void tour(CommonGBChannel channel, FrontEndControlCodeForTour frontEndControlCode, ErrorCallback<String> callback);

    void scan(CommonGBChannel channel, FrontEndControlCodeForScan frontEndControlCode, ErrorCallback<String> callback);

    void auxiliary(CommonGBChannel channel, FrontEndControlCodeForAuxiliary frontEndControlCode, ErrorCallback<String> callback);

    void wiper(CommonGBChannel channel, FrontEndControlCodeForWiper frontEndControlCode, ErrorCallback<String> callback);

    void queryPreset(CommonGBChannel channel, ErrorCallback<List<Preset>> callback);
}
