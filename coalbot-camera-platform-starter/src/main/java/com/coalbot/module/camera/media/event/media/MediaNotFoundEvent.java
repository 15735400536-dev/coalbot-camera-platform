package com.coalbot.module.camera.media.event.media;

import com.coalbot.module.camera.media.abl.bean.hook.ABLHookParam;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.zlm.dto.hook.OnStreamNotFoundHookParam;

/**
 * 流未找到
 */
public class MediaNotFoundEvent extends MediaEvent {
    public MediaNotFoundEvent(Object source) {
        super(source);
    }

    public static MediaNotFoundEvent getInstance(Object source, OnStreamNotFoundHookParam hookParam, MediaServer mediaServer){
        MediaNotFoundEvent mediaDepartureEven = new MediaNotFoundEvent(source);
        mediaDepartureEven.setApp(hookParam.getApp());
        mediaDepartureEven.setStream(hookParam.getStream());
        mediaDepartureEven.setSchema(hookParam.getSchema());
        mediaDepartureEven.setMediaServer(mediaServer);
        mediaDepartureEven.setParams(hookParam.getParams());
        return mediaDepartureEven;
    }

    public static MediaNotFoundEvent getInstance(Object source, ABLHookParam hookParam, MediaServer mediaServer){
        MediaNotFoundEvent mediaDepartureEven = new MediaNotFoundEvent(source);
        mediaDepartureEven.setApp(hookParam.getApp());
        mediaDepartureEven.setStream(hookParam.getStream());
        mediaDepartureEven.setMediaServer(mediaServer);
        return mediaDepartureEven;
    }
}
