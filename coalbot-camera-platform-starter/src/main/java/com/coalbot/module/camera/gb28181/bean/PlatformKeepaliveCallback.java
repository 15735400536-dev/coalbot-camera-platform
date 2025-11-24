package com.coalbot.module.camera.gb28181.bean;

public interface PlatformKeepaliveCallback {
    public void run(String platformServerGbId, int failCount);
}
