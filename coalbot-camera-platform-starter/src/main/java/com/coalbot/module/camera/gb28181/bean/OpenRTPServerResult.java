package com.coalbot.module.camera.gb28181.bean;

import com.coalbot.module.camera.media.event.hook.HookData;
import com.coalbot.module.camera.service.bean.SSRCInfo;
import lombok.Data;

@Data
public class OpenRTPServerResult {

    private SSRCInfo ssrcInfo;
    private HookData hookData;
}
