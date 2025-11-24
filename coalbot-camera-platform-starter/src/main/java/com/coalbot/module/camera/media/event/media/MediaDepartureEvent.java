package com.coalbot.module.camera.media.event.media;

import com.coalbot.module.camera.media.abl.bean.hook.ABLHookParam;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.zlm.dto.hook.OnStreamChangedHookParam;

/**
 * 流离开事件
 */
public class MediaDepartureEvent extends MediaEvent {
    public MediaDepartureEvent(Object source) {
        super(source);
    }

    public static MediaDepartureEvent getInstance(Object source, OnStreamChangedHookParam hookParam, MediaServer mediaServer){
        MediaDepartureEvent mediaDepartureEven = new MediaDepartureEvent(source);
        mediaDepartureEven.setApp(hookParam.getApp());
        mediaDepartureEven.setStream(hookParam.getStream());
        mediaDepartureEven.setSchema(hookParam.getSchema());
        mediaDepartureEven.setMediaServer(mediaServer);
        return mediaDepartureEven;
    }

    public static MediaDepartureEvent getInstance(Object source, ABLHookParam hookParam, MediaServer mediaServer){
        MediaDepartureEvent mediaDepartureEven = new MediaDepartureEvent(source);
        mediaDepartureEven.setApp(hookParam.getApp());
        mediaDepartureEven.setStream(hookParam.getStream());
        mediaDepartureEven.setMediaServer(mediaServer);
        return mediaDepartureEven;
    }
}
