package com.coalbot.module.camera.streamProxy.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.common.enums.ChannelDataType;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.service.IGbChannelService;
import com.coalbot.module.camera.mapper.streamProxy.StreamProxyMapper;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.event.media.MediaArrivalEvent;
import com.coalbot.module.camera.media.event.media.MediaDepartureEvent;
import com.coalbot.module.camera.media.event.media.MediaNotFoundEvent;
import com.coalbot.module.camera.media.event.mediaServer.MediaServerOfflineEvent;
import com.coalbot.module.camera.media.event.mediaServer.MediaServerOnlineEvent;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.media.zlm.dto.hook.OriginType;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.storager.IRedisCatchStorage;
import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyPlayService;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyService;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.camera.vmanager.bean.ResourceBaseInfo;
import com.coalbot.module.core.exception.CommonException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 视频代理业务
 */
@Slf4j
@Service
public class StreamProxyServiceImpl implements IStreamProxyService {

    @Autowired
    private StreamProxyMapper streamProxyMapper;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private IStreamProxyPlayService playService;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private IGbChannelService gbChannelService;

    @Autowired
    DataSourceTransactionManager dataSourceTransactionManager;

    @Autowired
    TransactionDefinition transactionDefinition;

    /**
     * 流到来的处理
     */
    @Async("taskExecutor")
    @Transactional
    @EventListener
    public void onApplicationEvent(MediaArrivalEvent event) {
        if ("rtsp".equals(event.getSchema())) {
            streamChangeHandler(event.getApp(), event.getStream(), event.getMediaServer().getId(), true);
        }
    }

    /**
     * 流离开的处理
     */
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void onApplicationEvent(MediaDepartureEvent event) {
        if ("rtsp".equals(event.getSchema())) {
            streamChangeHandler(event.getApp(), event.getStream(), event.getMediaServer().getId(), false);
        }
    }

    /**
     * 流未找到的处理
     */
    @Async("taskExecutor")
    @EventListener
    public void onApplicationEvent(MediaNotFoundEvent event) {
        if ("rtp".equals(event.getApp())) {
            return;
        }
        // 拉流代理
        StreamProxy streamProxyByAppAndStream = getStreamProxyByAppAndStream(event.getApp(), event.getStream());
        if (streamProxyByAppAndStream != null && streamProxyByAppAndStream.isEnableDisableNoneReader()) {
            startByAppAndStream(event.getApp(), event.getStream(), ((code, msg, data) -> {
                log.info("[拉流代理] 自动点播成功， app： {}， stream: {}", event.getApp(), event.getStream());
            }));
        }
    }

    /**
     * 流媒体节点上线
     */
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void onApplicationEvent(MediaServerOnlineEvent event) {
        zlmServerOnline(event.getMediaServer());
    }

    /**
     * 流媒体节点离线
     */
    @Async("taskExecutor")
    @EventListener
    @Transactional
    public void onApplicationEvent(MediaServerOfflineEvent event) {
        zlmServerOffline(event.getMediaServer());
    }


    @Override
    @Transactional
    public void add(StreamProxy streamProxy) {
        StreamProxy streamProxyInDb = streamProxyMapper.selectOneByAppAndStream(streamProxy.getApp(), streamProxy.getStream());
        if (streamProxyInDb != null) {
            throw new CommonException("APP+STREAM已经存在");
        }
        if (streamProxy.getGbDeviceId() != null) {
            gbChannelService.add(streamProxy.buildCommonGBChannel());
        }
        streamProxy.setCreateTime(new Date());
        streamProxy.setUpdateTime(new Date());
        streamProxyMapper.add(streamProxy);
        streamProxy.setDataType(ChannelDataType.STREAM_PROXY);
        streamProxy.setDataDeviceId(streamProxy.getId());
    }

    @Override
    public void delete(String id) {
        StreamProxy streamProxy = getStreamProxy(id);
        if (streamProxy == null) {
            throw new CommonException("代理不存在");
        }
        delete(streamProxy);
    }

    private void delete(StreamProxy streamProxy) {
        AssertUtils.notNull(streamProxy, "代理不可为NULL");
        if (streamProxy.getPulling() != null && streamProxy.getPulling()) {
            playService.stopProxy(streamProxy);
        }
        if (streamProxy.getGbId() != null) {
            gbChannelService.delete(streamProxy.getGbId());
        }
        streamProxyMapper.delete(streamProxy.getId());
    }

    @Override
    @Transactional
    public void delteByAppAndStream(String app, String stream) {
        StreamProxy streamProxy = streamProxyMapper.selectOneByAppAndStream(app, stream);
        if (streamProxy == null) {
            throw new CommonException("代理不存在");
        }
        delete(streamProxy);
    }

    /**
     * 更新代理流
     */
    @Override
    public boolean update(StreamProxy streamProxy) {
        streamProxy.setUpdateTime(new Date());
        StreamProxy streamProxyInDb = streamProxyMapper.select(streamProxy.getId());
        if (streamProxyInDb == null) {
            throw new CommonException("代理不存在");
        }
        int updateResult = streamProxyMapper.update(streamProxy);
        if (updateResult > 0 && !ObjectUtils.isEmpty(streamProxy.getGbDeviceId())) {
            if (streamProxy.getGbId() != null) {
                gbChannelService.update(streamProxy.buildCommonGBChannel());
            } else {
                gbChannelService.add(streamProxy.buildCommonGBChannel());
            }
        }
        return true;
    }

    @Override
    public PageInfo<StreamProxy> getAll(Integer page, Integer count, String query, Boolean pulling, String mediaServerId) {
        PageHelper.startPage(page, count);
        if (query != null) {
            query = query.replaceAll("/", "//")
                    .replaceAll("%", "/%")
                    .replaceAll("_", "/_");
        }
        List<StreamProxy> all = streamProxyMapper.selectAll(query, pulling, mediaServerId);
        return new PageInfo<>(all);
    }


    @Override
    public void startByAppAndStream(String app, String stream, ErrorCallback<StreamInfo> callback) {
        StreamProxy streamProxy = streamProxyMapper.selectOneByAppAndStream(app, stream);
        if (streamProxy == null) {
            throw new CommonException("代理信息未找到");
        }
        playService.startProxy(streamProxy, callback);
    }

    @Override
    public void stopByAppAndStream(String app, String stream) {
        StreamProxy streamProxy = streamProxyMapper.selectOneByAppAndStream(app, stream);
        if (streamProxy == null) {
            throw new CommonException("代理信息未找到");
        }
        playService.stopProxy(streamProxy);
    }


    @Override
    public Map<String, String> getFFmpegCMDs(MediaServer mediaServer) {
        return mediaServerService.getFFmpegCMDs(mediaServer);
    }


    @Override
    public StreamProxy getStreamProxyByAppAndStream(String app, String stream) {
        return streamProxyMapper.selectOneByAppAndStream(app, stream);
    }

    @Override
    @Transactional
    public void zlmServerOnline(MediaServer mediaServer) {
        if (mediaServer == null) {
            return;
        }
        // 这里主要是控制数据库/redis缓存/以及zlm中存在的代理流 三者状态一致。以数据库中数据为根本
        redisCatchStorage.removeStream(mediaServer.getId(), "PULL");

        List<StreamProxy> streamProxies = streamProxyMapper.selectForPushingInMediaServer(mediaServer.getId(), true);
        if (streamProxies.isEmpty()) {
            return;
        }
        Map<String, StreamProxy> streamProxyMapForDb = new HashMap<>();
        for (StreamProxy streamProxy : streamProxies) {
            streamProxyMapForDb.put(streamProxy.getApp() + "_" + streamProxy.getStream(), streamProxy);
        }

        List<StreamInfo> streamInfoList = mediaServerService.getMediaList(mediaServer, null, null, null);

        List<CommonGBChannel> channelListForOnline = new ArrayList<>();
        for (StreamInfo streamInfo : streamInfoList) {
            String key = streamInfo.getApp() + streamInfo.getStream();
            StreamProxy streamProxy = streamProxyMapForDb.get(key);
            if (streamProxy == null) {
                // 流媒体存在，数据库中不存在
                continue;
            }
            if (streamInfo.getOriginType() == OriginType.PULL.ordinal()
                    || streamInfo.getOriginType() == OriginType.FFMPEG_PULL.ordinal()) {
                if (streamProxyMapForDb.get(key) != null) {
                    redisCatchStorage.addStream(mediaServer, "pull", streamInfo.getApp(), streamInfo.getStream(), streamInfo.getMediaInfo());
                    if ("OFF".equalsIgnoreCase(streamProxy.getGbStatus()) && streamProxy.getGbId() != null) {
                        streamProxy.setGbStatus("ON");
                        channelListForOnline.add(streamProxy.buildCommonGBChannel());
                    }
                    streamProxyMapForDb.remove(key);
                }
            }
        }

        if (!channelListForOnline.isEmpty()) {
            gbChannelService.online(channelListForOnline, true);
        }
        List<CommonGBChannel> channelListForOffline = new ArrayList<>();
        List<StreamProxy> streamProxiesForRemove = new ArrayList<>();
        if (!streamProxyMapForDb.isEmpty()) {
            for (StreamProxy streamProxy : streamProxyMapForDb.values()) {
                if ("ON".equalsIgnoreCase(streamProxy.getGbStatus()) && streamProxy.getGbId() != null) {
                    streamProxy.setGbStatus("OFF");
                    channelListForOffline.add(streamProxy.buildCommonGBChannel());
                }
            }
        }
        if (!channelListForOffline.isEmpty()) {
            gbChannelService.offline(channelListForOffline, true);
        }
        if (!streamProxiesForRemove.isEmpty()) {
            streamProxyMapper.deleteByList(streamProxiesForRemove);
        }

        if (!streamProxyMapForDb.isEmpty()) {
            for (StreamProxy streamProxy : streamProxyMapForDb.values()) {
                streamProxyMapper.offline(streamProxy.getId());
            }
        }
    }

    @Override
    public void zlmServerOffline(MediaServer mediaServer) {
        List<StreamProxy> streamProxies = streamProxyMapper.selectForPushingInMediaServer(mediaServer.getId(), true);

        // 清理redis相关的缓存
        redisCatchStorage.removeStream(mediaServer.getId(), "PULL");

        if (streamProxies.isEmpty()) {
            return;
        }
        List<StreamProxy> streamProxiesForSendMessage = new ArrayList<>();
        List<CommonGBChannel> channelListForOffline = new ArrayList<>();

        for (StreamProxy streamProxy : streamProxies) {
            if (streamProxy.getGbId() != null && "ON".equalsIgnoreCase(streamProxy.getGbStatus())) {
                channelListForOffline.add(streamProxy.buildCommonGBChannel());
            }
            if ("ON".equalsIgnoreCase(streamProxy.getGbStatus())) {
                streamProxiesForSendMessage.add(streamProxy);
            }
        }
        if (!channelListForOffline.isEmpty()) {
            // 修改国标关联的国标通道的状态
            gbChannelService.offline(channelListForOffline, true);
        }
        if (!streamProxiesForSendMessage.isEmpty()) {
            for (StreamProxy streamProxy : streamProxiesForSendMessage) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("serverId", userSetting.getServerId());
                jsonObject.put("app", streamProxy.getApp());
                jsonObject.put("stream", streamProxy.getStream());
                jsonObject.put("register", false);
                jsonObject.put("mediaServerId", mediaServer);
                redisCatchStorage.sendStreamChangeMsg("pull", jsonObject);
            }
        }
    }

    @Transactional
    public void streamChangeHandler(String app, String stream, String mediaServerId, boolean status) {
        // 状态变化时推送到国标上级
        StreamProxy streamProxy = streamProxyMapper.selectOneByAppAndStream(app, stream);
        if (streamProxy == null) {
            return;
        }
        streamProxy.setPulling(status);
        streamProxy.setMediaServerId(mediaServerId);
        streamProxy.setUpdateTime(new Date());
        streamProxyMapper.updateStream(streamProxy);
    }

    @Override
    public ResourceBaseInfo getOverview() {

        int total = streamProxyMapper.getAllCount();
        int online = streamProxyMapper.getOnline();

        return new ResourceBaseInfo(total, online);
    }

    @Override
    public StreamProxy getStreamProxy(String id) {
        return streamProxyMapper.select(id);
    }

}
