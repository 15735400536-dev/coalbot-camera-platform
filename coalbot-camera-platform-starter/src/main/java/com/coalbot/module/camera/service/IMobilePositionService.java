package com.coalbot.module.camera.service;


import com.coalbot.module.camera.gb28181.bean.MobilePosition;
import com.coalbot.module.camera.gb28181.bean.Platform;

import java.util.List;

public interface IMobilePositionService {

    void add(List<MobilePosition> mobilePositionList);

    void add(MobilePosition mobilePosition);

    List<MobilePosition> queryMobilePositions(String deviceId, String channelId, String startTime, String endTime);

    List<Platform> queryEnablePlatformListWithAsMessageChannel();

    MobilePosition queryLatestPosition(String deviceId);

}
