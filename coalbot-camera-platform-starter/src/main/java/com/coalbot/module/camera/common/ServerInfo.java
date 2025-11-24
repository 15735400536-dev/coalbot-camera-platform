package com.coalbot.module.camera.common;

import com.coalbot.module.camera.utils.DateUtil;
import lombok.Data;

@Data
public class ServerInfo {

    private String ip;
    private int port;
    private String createTime;

    public static ServerInfo create(String ip, int port) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setIp(ip);
        serverInfo.setPort(port);
        serverInfo.setCreateTime(DateUtil.getNow());
        return serverInfo;
    }
}
