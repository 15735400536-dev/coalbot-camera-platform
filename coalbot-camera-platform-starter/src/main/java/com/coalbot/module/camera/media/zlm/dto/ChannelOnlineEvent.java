package com.coalbot.module.camera.media.zlm.dto;

import com.coalbot.module.camera.gb28181.bean.SendRtpInfo;

import java.text.ParseException;

/**
 * @author lin
 */
public interface ChannelOnlineEvent {

    void run(SendRtpInfo sendRtpItem) throws ParseException;
}
