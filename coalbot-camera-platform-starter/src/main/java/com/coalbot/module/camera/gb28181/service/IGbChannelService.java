package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.gb28181.bean.*;
import com.coalbot.module.camera.gb28181.controller.bean.Extent;
import com.coalbot.module.camera.service.bean.GPSMsgInfo;
import com.coalbot.module.camera.streamPush.bean.StreamPush;
import com.github.pagehelper.PageInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface IGbChannelService {

    CommonGBChannel queryByDeviceId(String gbDeviceId);

    int add(CommonGBChannel commonGBChannel);

    int delete(String gbId);

    void delete(Collection<String> ids);

    int update(CommonGBChannel commonGBChannel);

    int offline(CommonGBChannel commonGBChannel);

    int offline(List<CommonGBChannel> commonGBChannelList, boolean permission);

    int online(CommonGBChannel commonGBChannel);

    int online(List<CommonGBChannel> commonGBChannelList, boolean permission);

    void batchAdd(List<CommonGBChannel> commonGBChannels);

    void updateStatus(List<CommonGBChannel> channelList);

    CommonGBChannel getOne(String id);

    List<IndustryCodeType> getIndustryCodeList();

    List<DeviceType> getDeviceTypeList();

    List<NetworkIdentificationType> getNetworkIdentificationTypeList();

    void reset(String id, List<String> chanelFields);

    PageInfo<CommonGBChannel> queryListByCivilCode(int page, int count, String query, Boolean online, Integer channelType, String civilCode);

    PageInfo<CommonGBChannel> queryListByParentId(int page, int count, String query, Boolean online, Integer channelType, String groupDeviceId);

    void removeCivilCode(List<Region> allChildren);

    void addChannelToRegion(String civilCode, List<String> channelIds);

    void deleteChannelToRegion(String civilCode, List<String> channelIds);

    void deleteChannelToRegionByCivilCode(String civilCode);

    void deleteChannelToRegionByChannelIds(List<String> channelIds);

    void addChannelToRegionByGbDevice(String civilCode, List<String> deviceIds);

    void deleteChannelToRegionByGbDevice(List<String> deviceIds);

    void removeParentIdByBusinessGroup(String businessGroup);

    void removeParentIdByGroupList(List<Group> groupList);

    void updateBusinessGroup(String oldBusinessGroup, String newBusinessGroup);

    void updateParentIdGroup(String oldParentId, String newParentId);

    void addChannelToGroup(String parentId, String businessGroup, List<String> channelIds);

    void deleteChannelToGroup(String parentId, String businessGroup, List<String> channelIds);

    void addChannelToGroupByGbDevice(String parentId, String businessGroup, List<String> deviceIds);

    void deleteChannelToGroupByGbDevice(List<String> deviceIds);

    void batchUpdateForStreamPushRedisMsg(List<CommonGBChannel> commonGBChannels, boolean permission);

    CommonGBChannel queryOneWithPlatform(String platformId, String channelDeviceId);

    void updateCivilCode(String oldCivilCode, String newCivilCode);

    List<CommonGBChannel> queryListByStreamPushList(List<StreamPush> streamPushList);

    PageInfo<CommonGBChannel> queryList(int page, int count,
                                        String query, Boolean online, Boolean hasRecordPlan, Integer channelType, String civilCode, String parentDeviceId);

    PageInfo<CommonGBChannel> queryListByCivilCodeForUnusual(int page, int count, String query, Boolean online, Integer channelType);

    void clearChannelCivilCode(Boolean all, List<String> channelIds);

    PageInfo<CommonGBChannel> queryListByParentForUnusual(int page, int count, String query, Boolean online, Integer channelType);

    void clearChannelParent(Boolean all, List<String> channelIds);

    void updateGPSFromGPSMsgInfo(List<GPSMsgInfo> gpsMsgInfoList);

    void updateGPS(List<CommonGBChannel> channelList);

    List<CommonGBChannel> queryListForMap(String query, Boolean online, Boolean hasRecordPlan, Integer channelType);

    CommonGBChannel queryCommonChannelByDeviceChannel(DeviceChannel channel);

    void resetLevel();

    byte[] getTile(int z, int x, int y, String geoCoordSys);

    String drawThin(Map<Integer, Double> zoomParam, Extent extent, String geoCoordSys);

    DrawThinProcess thinProgress(String id);

    void saveThin(String id);
}
