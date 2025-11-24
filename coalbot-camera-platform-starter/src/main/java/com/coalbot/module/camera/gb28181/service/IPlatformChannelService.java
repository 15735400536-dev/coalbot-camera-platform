package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.gb28181.bean.*;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 平台关联通道管理
 * @author lin
 */
public interface IPlatformChannelService {

    PageInfo<PlatformChannel> queryChannelList(int page, int count, String query, Integer channelType, Boolean online, String platformId, Boolean hasShare);

    int addAllChannel(String platformId);

    int removeAllChannel(String platformId);

    int addChannels(String platformId, List<String> channelIds);

    int removeChannels(String platformId, List<String> channelIds);

    void removeChannels(List<String> ids);

    void removeChannel(String gbId);

    List<CommonGBChannel> queryByPlatform(Platform platform);

    void pushChannel(String platformId);

    void addChannelByDevice(String platformId, List<String> deviceIds);

    void removeChannelByDevice(String platformId, List<String> deviceIds);

    void updateCustomChannel(PlatformChannel channel);

    void checkGroupRemove(List<CommonGBChannel> channelList, List<Group> groups);

    void checkGroupAdd(List<CommonGBChannel> channelList);

    List<Platform> queryPlatFormListByChannelDeviceId(String channelId, List<String> platforms);

    CommonGBChannel queryChannelByPlatformIdAndChannelId(String platformId, String channelId);

    List<CommonGBChannel> queryChannelByPlatformIdAndChannelIds(String platformId, List<String> channelIds);

    void checkRegionAdd(List<CommonGBChannel> channelList);

    void checkRegionRemove(List<CommonGBChannel> channelList, List<Region> regionList);

    List<Platform> queryByPlatformBySharChannelId(String gbId);
}
