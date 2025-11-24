package com.coalbot.module.camera.service;

import com.coalbot.module.camera.gb28181.bean.OpenRTPServerResult;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.service.bean.RTPServerParam;
import com.coalbot.module.camera.service.bean.SSRCInfo;

public interface IReceiveRtpServerService {
    SSRCInfo openRTPServer(RTPServerParam rtpServerParam, ErrorCallback<OpenRTPServerResult> callback);

    void closeRTPServer(MediaServer mediaServer, SSRCInfo ssrcInfo);
}
