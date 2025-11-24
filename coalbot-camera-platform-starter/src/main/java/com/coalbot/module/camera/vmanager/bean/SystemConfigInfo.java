package com.coalbot.module.camera.vmanager.bean;

import com.coalbot.module.camera.common.VersionPo;
import com.coalbot.module.camera.conf.SipConfig;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.jt1078.config.JT1078Config;
import lombok.Data;

@Data
public class SystemConfigInfo {

    private int serverPort;
    private SipConfig sip;
    private UserSetting addOn;
    private VersionPo version;
    private JT1078Config jt1078Config;

}

