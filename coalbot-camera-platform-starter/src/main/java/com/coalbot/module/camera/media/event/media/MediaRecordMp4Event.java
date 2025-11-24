package com.coalbot.module.camera.media.event.media;

import com.coalbot.module.camera.media.abl.ABLHttpHookListener;
import com.coalbot.module.camera.media.abl.bean.hook.OnRecordMp4ABLHookParam;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.bean.RecordInfo;
import com.coalbot.module.camera.media.zlm.dto.hook.OnRecordMp4HookParam;

/**
 * 录像文件生成事件
 */
public class MediaRecordMp4Event extends MediaEvent {
    public MediaRecordMp4Event(Object source) {
        super(source);
    }

    private RecordInfo recordInfo;

    public static MediaRecordMp4Event getInstance(Object source, OnRecordMp4HookParam hookParam, MediaServer mediaServer){
        MediaRecordMp4Event mediaRecordMp4Event = new MediaRecordMp4Event(source);
        mediaRecordMp4Event.setApp(hookParam.getApp());
        mediaRecordMp4Event.setStream(hookParam.getStream());
        RecordInfo recordInfo = RecordInfo.getInstance(hookParam);
        mediaRecordMp4Event.setRecordInfo(recordInfo);
        mediaRecordMp4Event.setMediaServer(mediaServer);
        return mediaRecordMp4Event;
    }

    public static MediaRecordMp4Event getInstance(ABLHttpHookListener source, OnRecordMp4ABLHookParam hookParam, MediaServer mediaServer) {
        MediaRecordMp4Event mediaRecordMp4Event = new MediaRecordMp4Event(source);
        mediaRecordMp4Event.setApp(hookParam.getApp());
        mediaRecordMp4Event.setStream(hookParam.getStream());
        RecordInfo recordInfo = RecordInfo.getInstance(hookParam);
        mediaRecordMp4Event.setRecordInfo(recordInfo);
        mediaRecordMp4Event.setMediaServer(mediaServer);
        return mediaRecordMp4Event;
    }

    public RecordInfo getRecordInfo() {
        return recordInfo;
    }

    public void setRecordInfo(RecordInfo recordInfo) {
        this.recordInfo = recordInfo;
    }

}
