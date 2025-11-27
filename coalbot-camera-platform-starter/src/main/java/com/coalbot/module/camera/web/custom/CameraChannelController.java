package com.coalbot.module.camera.web.custom;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.conf.DynamicTask;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.dto.CameraChannelQueryDTO;
import com.coalbot.module.camera.dto.CloudRecordUrlQueryDTO;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.media.zlm.dto.StreamAuthorityInfo;
import com.coalbot.module.camera.service.ICloudRecordService;
import com.coalbot.module.camera.service.bean.CloudRecordItem;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.service.bean.InviteErrorCode;
import com.coalbot.module.camera.storager.IRedisCatchStorage;
import com.coalbot.module.camera.streamPush.bean.StreamPush;
import com.coalbot.module.camera.streamPush.service.IStreamPushPlayService;
import com.coalbot.module.camera.streamPush.service.IStreamPushService;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.camera.utils.DateUtil;
import com.coalbot.module.camera.utils.HttpUtils;
import com.coalbot.module.camera.utils.TypeUtils;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.camera.vmanager.bean.StreamContent;
import com.coalbot.module.camera.vmanager.cloudRecord.bean.CloudRecordUrl;
import com.coalbot.module.camera.web.custom.bean.*;
import com.coalbot.module.camera.web.custom.service.CameraChannelService;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.PageList;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Tag(name = "第三方接口")
@Slf4j
@RestController
@RequestMapping(value = "/api/sy")
@ConditionalOnProperty(value = "sy.enable", havingValue = "true")
@Hidden
public class CameraChannelController {

    @Autowired
    private CameraChannelService channelService;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private ICloudRecordService cloudRecordService;

    @Autowired
    private IStreamPushPlayService streamPushPlayService;

    @Autowired
    private DynamicTask dynamicTask;

    @Autowired
    private IStreamPushService streamPushService;

    @Value("${sy.ptz-control-time-interval}")
    private int ptzControlTimeInterval = 300;

    @PostMapping(value = "/camera/list")
    @ResponseBody
    @Operation(summary = "查询摄像机列表, 只查询当前虚拟组织下的")
    public RetResult<PageList<CameraChannel>> queryList(@RequestBody CameraChannelQueryDTO param) {
        PageInfo<CameraChannel> pageResult = channelService.queryList(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getSize()),
                param.getGroupAlias(), param.getStatus(), param.getGeoCoordSys());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @PostMapping(value = "/camera/list-with-child")
    @ResponseBody
    @Operation(summary = "查询摄像机列表, 查询当前虚拟组织下以及全部子节点")
    public RetResult<PageList<CameraChannel>> queryListWithChild(@RequestBody CameraChannelQueryDTO param) {
        PageInfo<CameraChannel> pageResult = channelService.queryListWithChild(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getSize()),
                param.getQuery(), param.getSortName(), param.getSort(), param.getGroupAlias(), param.getStatus(), param.getGeoCoordSys());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @GetMapping(value = "/camera/cont-with-child")
    @ResponseBody
    @Operation(summary = "查询摄像机列表的总数和在线数")
    @Parameter(name = "groupAlias", description = "分组别名")
    public List<CameraCount> queryCountWithChild(String groupAlias) {
        return channelService.queryCountWithChild(groupAlias);
    }

    @GetMapping(value = "/camera/one")
    @ResponseBody
    @Operation(summary = "查询单个摄像头信息")
    @Parameter(name = "deviceId", description = "通道编号")
    @Parameter(name = "deviceCode", description = "摄像头设备国标编号, 对于非国标摄像头可以不设置此参数")
    @Parameter(name = "geoCoordSys", description = "坐标系类型：WGS84,GCJ02、BD09")
    public CameraChannel getOne(String deviceId, @RequestParam(required = false) String deviceCode,
                                @RequestParam(required = false) String geoCoordSys) {
        return channelService.queryOne(deviceId, deviceCode, geoCoordSys);
    }

    @GetMapping(value = "/camera/update")
    @ResponseBody
    @Operation(summary = "更新摄像头信息")
    @Parameter(name = "deviceId", description = "通道编号")
    @Parameter(name = "deviceCode", description = "摄像头设备国标编号, 对于非国标摄像头可以不设置此参数")
    @Parameter(name = "name", description = "通道名称")
    @Parameter(name = "longitude", description = "经度")
    @Parameter(name = "latitude", description = "纬度")
    @Parameter(name = "geoCoordSys", description = "坐标系类型：WGS84,GCJ02、BD09")
    public RetResult<Void> updateCamera(String deviceId,
                                        @RequestParam(required = false) String deviceCode,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) Double longitude,
                                        @RequestParam(required = false) Double latitude,
                                        @RequestParam(required = false) String geoCoordSys) {
        channelService.updateCamera(deviceId, deviceCode, name, longitude, latitude, geoCoordSys);
        return RetResponse.makeOKRsp();
    }

    @PostMapping(value = "/camera/list/ids")
    @ResponseBody
    @Operation(summary = "根据编号查询多个摄像头信息")
    public List<CameraChannel> queryListByDeviceIds(@RequestBody IdsQueryParam param) {
        return channelService.queryListByDeviceIds(param.getDeviceIds(), param.getGeoCoordSys());
    }

    @GetMapping(value = "/camera/list/box")
    @ResponseBody
    @Operation(summary = "根据矩形查询摄像头")
    @Parameter(name = "minLongitude", description = "最小经度")
    @Parameter(name = "maxLongitude", description = "最大经度")
    @Parameter(name = "minLatitude", description = "最小纬度")
    @Parameter(name = "maxLatitude", description = "最大纬度")
    @Parameter(name = "level", description = "地图级别")
    @Parameter(name = "groupAlias", description = "分组别名")
    @Parameter(name = "geoCoordSys", description = "坐标系类型：WGS84,GCJ02、BD09")
    public List<CameraChannel> queryListInBox(Double minLongitude, Double maxLongitude,
                                              Double minLatitude, Double maxLatitude,
                                              @RequestParam(required = false) Integer level,
                                              String groupAlias,
                                              @RequestParam(required = false) String geoCoordSys) {
        return channelService.queryListInBox(minLongitude, maxLongitude, minLatitude, maxLatitude, level, groupAlias, geoCoordSys);
    }

    @PostMapping(value = "/camera/list/polygon")
    @ResponseBody
    @Operation(summary = "根据多边形查询摄像头")
    public List<CameraChannel> queryListInPolygon(@RequestBody PolygonQueryParam param) {
        return channelService.queryListInPolygon(param.getPosition(), param.getGroupAlias(), param.getLevel(), param.getGeoCoordSys());
    }

    @GetMapping(value = "/camera/list/circle")
    @ResponseBody
    @Operation(summary = "根据圆范围查询摄像头")
    @Parameter(name = "centerLongitude", description = "圆心经度")
    @Parameter(name = "centerLatitude", description = "圆心纬度")
    @Parameter(name = "radius", description = "查询范围的半径，单位米")
    @Parameter(name = "level", description = "地图级别")
    @Parameter(name = "groupAlias", description = "分组别名")
    @Parameter(name = "geoCoordSys", description = "坐标系类型：WGS84,GCJ02、BD09")
    public List<CameraChannel> queryListInCircle(Double centerLongitude, Double centerLatitude, Double radius, String groupAlias,
                                                 @RequestParam(required = false) String geoCoordSys, @RequestParam(required = false) Integer level) {
        return channelService.queryListInCircle(centerLongitude, centerLatitude, radius, level, groupAlias, geoCoordSys);
    }

    @GetMapping(value = "/camera/list/address")
    @ResponseBody
    @Operation(summary = "根据安装地址和监视方位获取摄像头")
    @Parameter(name = "address", description = "安装地址")
    @Parameter(name = "directionType", description = "监视方位", required = false)
    @Parameter(name = "geoCoordSys", description = "坐标系类型：WGS84,GCJ02、BD09")
    public List<CameraChannel> queryListByAddressAndDirectionType(String address, @RequestParam(required = false) Integer directionType, @RequestParam(required = false) String geoCoordSys) {
        return channelService.queryListByAddressAndDirectionType(address, directionType, geoCoordSys);
    }

    @GetMapping(value = "/camera/control/play")
    @ResponseBody
    @Operation(summary = "播放摄像头")
    @Parameter(name = "deviceId", description = "通道编号")
    @Parameter(name = "deviceCode", description = "摄像头设备国标编号, 对于非国标摄像头可以不设置此参数")
    public DeferredResult<RetResult<CameraStreamContent>> play(HttpServletRequest request, String deviceId, @RequestParam(required = false) String deviceCode) {

        log.info("[SY-播放摄像头] API调用，deviceId：{} ，deviceCode：{} ", deviceId, deviceCode);
        DeferredResult<RetResult<CameraStreamContent>> result = new DeferredResult<>(userSetting.getPlayTimeout().longValue());

        ErrorCallback<CameraStreamInfo> callback = (code, msg, cameraStreamInfo) -> {
            if (code == InviteErrorCode.SUCCESS.getCode()) {
                StreamInfo streamInfo = cameraStreamInfo.getStreamInfo();
                CommonGBChannel channel = cameraStreamInfo.getChannel();
                RetResult<CameraStreamContent> RetResult = RetResponse.makeOKRsp();
                if (cameraStreamInfo.getStreamInfo() != null) {
                    if (userSetting.getUseSourceIpAsStreamIp()) {
                        streamInfo = streamInfo.clone();//深拷贝
                        String host;
                        try {
                            URL url = new URL(request.getRequestURL().toString());
                            host = url.getHost();
                        } catch (MalformedURLException e) {
                            host = request.getLocalAddr();
                        }
                        streamInfo.changeStreamIp(host);
                    }
                    if (!ObjectUtils.isEmpty(streamInfo.getMediaServer().getTranscodeSuffix())
                            && !"null".equalsIgnoreCase(streamInfo.getMediaServer().getTranscodeSuffix())) {
                        streamInfo.setStream(streamInfo.getStream() + "_" + streamInfo.getMediaServer().getTranscodeSuffix());
                    }
                    CameraStreamContent cameraStreamContent = new CameraStreamContent(streamInfo);
                    cameraStreamContent.setName(channel.getGbName());
                    if (channel.getGbPtzType() != null) {
                        cameraStreamContent.setControlType(
                                (channel.getGbPtzType() == 1 || channel.getGbPtzType() == 4 || channel.getGbPtzType() == 5) ? 1 : 0);
                    } else {
                        cameraStreamContent.setControlType(0);
                    }

                    RetResult.setData(cameraStreamContent);
                } else {
                    RetResult.setCode(code);
                    RetResult.setMsg(msg);
                }
                result.setResult(RetResult);
            } else {
                result.setResult(RetResponse.makeRsp(code, msg));
            }
        };
        channelService.play(deviceId, deviceCode, callback);
        return result;
    }

    @GetMapping(value = "/camera/control/stop")
    @ResponseBody
    @Operation(summary = "停止播放摄像头")
    @Parameter(name = "deviceId", description = "通道编号")
    @Parameter(name = "deviceCode", description = "摄像头设备国标编号, 对于非国标摄像头可以不设置此参数")
    public RetResult<Void> stopPlay(String deviceId, @RequestParam(required = false) String deviceCode) {
        log.info("[SY-停止播放摄像头] API调用，deviceId：{} ，deviceCode：{} ", deviceId, deviceCode);
        channelService.stopPlay(deviceId, deviceCode);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "云台控制")
    @Parameter(name = "deviceId", description = "通道编号")
    @Parameter(name = "deviceCode", description = "摄像头设备国标编号, 对于非国标摄像头可以不设置此参数")
    @Parameter(name = "command", description = "控制指令,允许值: left, right, up, down, upleft, upright, downleft, downright, zoomin, zoomout, stop", required = true)
    @Parameter(name = "speed", description = "速度(0-100)", required = true)
    @GetMapping("/camera/control/ptz")
    public DeferredResult<RetResult<String>> ptz(String deviceId, @RequestParam(required = false) String deviceCode, String command, Integer speed) {

        log.info("[SY-云台控制] API调用，deviceId：{} ，deviceCode：{} ，command：{} ，speed：{} ", deviceId, deviceCode, command, speed);

        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(() -> {
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        channelService.ptz(deviceId, deviceCode, command, speed, (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        });
        // 设置时间间隔后自动发送停止
        if (!command.equalsIgnoreCase("stop")) {
            dynamicTask.startDelay(UUID.randomUUID().toString(), () -> {
                channelService.ptz(deviceId, deviceCode, "stop", speed, (code, msg, data) -> {
                });
            }, ptzControlTimeInterval);
        }
        return result;
    }

    @PostMapping(value = "/camera/list-for-mobile")
    @ResponseBody
    @Operation(summary = "查询移动设备摄像机列表")
    public RetResult<PageList<CameraChannel>> queryListForMobile(@RequestBody CameraChannelQueryDTO param) {
        PageInfo<CameraChannel> pageResult = channelService.queryListForMobile(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getSize()),
                param.getTopGroupAlias());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "获取推流播放地址")
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流id", required = true)
    @Parameter(name = "callId", description = "推流时携带的自定义鉴权ID", required = true)
    @GetMapping(value = "/push/play")
    @ResponseBody
    public DeferredResult<RetResult<StreamContent>> getStreamInfoByAppAndStream(HttpServletRequest request,
                                                                                String app,
                                                                                String stream,
                                                                                String callId) {
        StreamPush streamPush = streamPushService.getPush(app, stream);
        AssertUtils.notNull(streamPush, "地址不存在");

        // 权限校验
        StreamAuthorityInfo streamAuthorityInfo = redisCatchStorage.getStreamAuthorityInfo(app, stream);
        if (streamAuthorityInfo == null
                || streamAuthorityInfo.getCallId() == null
                || !streamAuthorityInfo.getCallId().equals(callId)) {
            throw new CommonException("播放地址鉴权失败");
        }

        DeferredResult<RetResult<StreamContent>> result = new DeferredResult<>(userSetting.getPlayTimeout().longValue());
        result.onTimeout(() -> {
            RetResult<StreamContent> fail = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "等待推流超时");
            result.setResult(fail);
        });

        streamPushPlayService.start(streamPush.getId(), (code, msg, streamInfo) -> {
            if (code == 0 && streamInfo != null) {
                streamInfo = streamInfo.clone();//深拷贝
                String host;
                try {
                    URL url = new URL(request.getRequestURL().toString());
                    host = url.getHost();
                } catch (MalformedURLException e) {
                    host = request.getLocalAddr();
                }
                streamInfo.changeStreamIp(host);
                RetResult<StreamContent> success = RetResponse.makeOKRsp(new StreamContent(streamInfo));
                result.setResult(success);
            }
        }, null, null);
        return result;
    }

    @Operation(summary = "获取推流播放地址（不做检查）")
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流id", required = true)
    @Parameter(name = "callId", description = "推流时携带的自定义鉴权ID", required = true)
    @GetMapping(value = "/push/play-without-check")
    @ResponseBody
    public RetResult<StreamContent> getStreamInfoByAppAndStreamWithoutCheck(HttpServletRequest request,
                                                                            String app,
                                                                            String stream,
                                                                            String callId) {

        MediaServer mediaServer = mediaServerService.getDefaultMediaServer();
        AssertUtils.notNull(mediaServer, "流媒体服务器不存在");
        StreamInfo streamInfo = mediaServerService.getStreamInfoByAppAndStream(mediaServer, app, stream, null, callId);
        streamInfo = streamInfo.clone();//深拷贝
        String host;
        try {
            URL url = new URL(request.getRequestURL().toString());
            host = url.getHost();
        } catch (MalformedURLException e) {
            host = request.getLocalAddr();
        }
        streamInfo.changeStreamIp(host);
        return RetResponse.makeOKRsp(new StreamContent(streamInfo));
    }

    @ResponseBody
    @GetMapping("/record/collect/add")
    @Operation(summary = "添加收藏")
    @Parameter(name = "app", description = "应用名", required = false)
    @Parameter(name = "stream", description = "流ID", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = false)
    @Parameter(name = "startTime", description = "鉴权ID", required = false)
    @Parameter(name = "endTime", description = "鉴权ID", required = false)
    @Parameter(name = "callId", description = "鉴权ID", required = false)
    @Parameter(name = "recordId", description = "录像记录的ID，用于精准收藏一个视频文件", required = false)
    public RetResult<Integer> addCollect(@RequestParam(required = false) String app, @RequestParam(required = false) String stream, @RequestParam(required = false) String mediaServerId, @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime, @RequestParam(required = false) String callId, @RequestParam(required = false) String recordId) {
        log.info("[云端录像] 添加收藏，app={}，stream={},mediaServerId={},startTime={},endTime={},callId={},recordId={}", app, stream, mediaServerId, startTime, endTime, callId, recordId);
        if (recordId != null) {
            return RetResponse.makeOKRsp(cloudRecordService.changeCollectById(recordId, true));
        } else {
            return RetResponse.makeOKRsp(cloudRecordService.changeCollect(true, app, stream, mediaServerId, startTime, endTime, callId));
        }
    }

    @ResponseBody
    @GetMapping("/record/collect/delete")
    @Operation(summary = "移除收藏")
    @Parameter(name = "app", description = "应用名", required = false)
    @Parameter(name = "stream", description = "流ID", required = false)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = false)
    @Parameter(name = "startTime", description = "鉴权ID", required = false)
    @Parameter(name = "endTime", description = "鉴权ID", required = false)
    @Parameter(name = "callId", description = "鉴权ID", required = false)
    @Parameter(name = "recordId", description = "录像记录的ID，用于精准精准移除一个视频文件的收藏", required = false)
    public RetResult<Integer> deleteCollect(@RequestParam(required = false) String app, @RequestParam(required = false) String stream, @RequestParam(required = false) String mediaServerId, @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime, @RequestParam(required = false) String callId, @RequestParam(required = false) String recordId) {
        log.info("[云端录像] 移除收藏，app={}，stream={},mediaServerId={},startTime={},endTime={},callId={},recordId={}", app, stream, mediaServerId, startTime, endTime, callId, recordId);
        if (recordId != null) {
            return RetResponse.makeOKRsp(cloudRecordService.changeCollectById(recordId, false));
        } else {
            return RetResponse.makeOKRsp(cloudRecordService.changeCollect(false, app, stream, mediaServerId, startTime, endTime, callId));
        }
    }

    /************************* 以下这些接口只适合wvp和zlm部署在同一台服务器的情况，且wvp只有一个zlm节点的情况 ***************************************/

    /**
     * 下载指定录像文件的压缩包
     *
     * @param app    应用名
     * @param stream 流ID
     * @param callId 每次录像的唯一标识，置空则查询全部流媒体
     */
    @ResponseBody
    @GetMapping("/record/zip")
    public RetResult<Void> downloadZipFile(HttpServletResponse response,
                                           @RequestParam(required = false) String app,
                                           @RequestParam(required = false) String stream,
                                           @RequestParam(required = false) String callId

    ) {
        log.info("[下载指定录像文件的压缩包] 查询 app->{}, stream->{}, callId->{}", app, stream, callId);

        if (app != null && ObjectUtils.isEmpty(app.trim())) {
            app = null;
        }
        if (stream != null && ObjectUtils.isEmpty(stream.trim())) {
            stream = null;
        }
        if (callId != null && ObjectUtils.isEmpty(callId.trim())) {
            callId = null;
        }
        // 设置响应头
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");
        if (stream != null && callId != null) {
            response.addHeader("Content-Disposition", "attachment;filename=" + stream + "_" + callId + ".zip");
        }
        List<CloudRecordUrl> cloudRecordItemList = cloudRecordService.getUrlList(app, stream, callId);
        if (ObjectUtils.isEmpty(cloudRecordItemList)) {
            log.warn("[下载指定录像文件的压缩包] 未找到录像文件，app->{}, stream->{}, callId->{}", app, stream, callId);
            return RetResponse.makeOKRsp();
        }

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (CloudRecordUrl recordUrl : cloudRecordItemList) {
                try {
                    zos.putNextEntry(new ZipEntry(recordUrl.getFileName()));
                    boolean downloadSuccess = HttpUtils.downLoadFile(recordUrl.getDownloadUrl(), zos);
                    if (!downloadSuccess) {
                        log.warn("[下载指定录像文件的压缩包] 下载文件失败: {}", recordUrl.getDownloadUrl());
                        zos.closeEntry();
                        continue;
                    }
                    zos.closeEntry();
                } catch (Exception e) {
                    log.error("[下载指定录像文件的压缩包] 处理文件失败: {}, 错误: {}", recordUrl.getFileName(), e.getMessage());
                    // 继续处理下一个文件
                }
            }
        } catch (IOException e) {
            log.error("[下载指定录像文件的压缩包] 创建压缩包失败，查询 app->{}, stream->{}, callId->{}", app, stream, callId, e);
        }
        return RetResponse.makeOKRsp();
    }

    @ResponseBody
    @PostMapping("/record/list-url")
    @Operation(summary = "分页查询云端录像")
    public RetResult<PageList<CloudRecordUrl>> getListWithUrl(HttpServletRequest request, @RequestBody CloudRecordUrlQueryDTO param) {
        log.info("[云端录像] 查询URL app->{}, stream->{}, mediaServerId->{}, page->{}, count->{}, startTime->{}, endTime->{}, callId->{}", param.getApp(), param.getStream(), param.getMediaServerId(), param.getCurrent(), param.getSize(), param.getStartTime(), param.getEndTime(), param.getCallId());

        List<MediaServer> mediaServers;
        if (!ObjectUtils.isEmpty(param.getMediaServerId())) {
            mediaServers = new ArrayList<>();
            MediaServer mediaServer = mediaServerService.getOne(param.getMediaServerId());
            if (mediaServer == null) {
                throw new CommonException("未找到流媒体: " + param.getMediaServerId());
            }
            mediaServers.add(mediaServer);
        } else {
            mediaServers = null;
        }
        MediaServer mediaServer = mediaServerService.getDefaultMediaServer();
        if (mediaServer == null) {
            throw new CommonException("未找到流媒体节点");
        }
        String remoteHost = param.getRemoteHost();
        if (remoteHost == null) {
            remoteHost = request.getScheme() + "://" + request.getLocalAddr() + ":" + (request.getScheme().equals("https") ? mediaServer.getHttpSSlPort() : mediaServer.getHttpPort());
        }
        PageInfo<CloudRecordItem> cloudRecordItemPageInfo = cloudRecordService.getList(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getSize()),
                param.getQuery(), param.getApp(), param.getStream(), param.getStartTime(), param.getEndTime(), mediaServers, param.getCallId(), null);
        PageInfo<CloudRecordUrl> cloudRecordUrlPageInfo = new PageInfo<>();
        if (!ObjectUtils.isEmpty(cloudRecordItemPageInfo)) {
            cloudRecordUrlPageInfo.setPageNum(cloudRecordItemPageInfo.getPageNum());
            cloudRecordUrlPageInfo.setPageSize(cloudRecordItemPageInfo.getPageSize());
            cloudRecordUrlPageInfo.setSize(cloudRecordItemPageInfo.getSize());
            cloudRecordUrlPageInfo.setEndRow(cloudRecordItemPageInfo.getEndRow());
            cloudRecordUrlPageInfo.setStartRow(cloudRecordItemPageInfo.getStartRow());
            cloudRecordUrlPageInfo.setPages(cloudRecordItemPageInfo.getPages());
            cloudRecordUrlPageInfo.setPrePage(cloudRecordItemPageInfo.getPrePage());
            cloudRecordUrlPageInfo.setNextPage(cloudRecordItemPageInfo.getNextPage());
            cloudRecordUrlPageInfo.setIsFirstPage(cloudRecordItemPageInfo.isIsFirstPage());
            cloudRecordUrlPageInfo.setIsLastPage(cloudRecordItemPageInfo.isIsLastPage());
            cloudRecordUrlPageInfo.setHasPreviousPage(cloudRecordItemPageInfo.isHasPreviousPage());
            cloudRecordUrlPageInfo.setHasNextPage(cloudRecordItemPageInfo.isHasNextPage());
            cloudRecordUrlPageInfo.setNavigatePages(cloudRecordItemPageInfo.getNavigatePages());
            cloudRecordUrlPageInfo.setNavigateFirstPage(cloudRecordItemPageInfo.getNavigateFirstPage());
            cloudRecordUrlPageInfo.setNavigateLastPage(cloudRecordItemPageInfo.getNavigateLastPage());
            cloudRecordUrlPageInfo.setNavigatepageNums(cloudRecordItemPageInfo.getNavigatepageNums());
            cloudRecordUrlPageInfo.setTotal(cloudRecordItemPageInfo.getTotal());
            List<CloudRecordUrl> cloudRecordUrlList = new ArrayList<>(cloudRecordItemPageInfo.getList().size());
            List<CloudRecordItem> cloudRecordItemList = cloudRecordItemPageInfo.getList();
            for (CloudRecordItem cloudRecordItem : cloudRecordItemList) {
                CloudRecordUrl cloudRecordUrl = new CloudRecordUrl();
                cloudRecordUrl.setId(cloudRecordItem.getId());
                cloudRecordUrl.setDownloadUrl(remoteHost + "/index/api/downloadFile?file_path=" + cloudRecordItem.getFilePath() + "&save_name=" + cloudRecordItem.getStream() + "_" + cloudRecordItem.getCallId() + "_" + DateUtil.timestampMsToUrlToyyyy_MM_dd_HH_mm_ss((long) cloudRecordItem.getStartTime()));
                cloudRecordUrl.setPlayUrl(remoteHost + "/index/api/downloadFile?file_path=" + cloudRecordItem.getFilePath());
                cloudRecordUrlList.add(cloudRecordUrl);
            }
            cloudRecordUrlPageInfo.setList(cloudRecordUrlList);
        }
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(cloudRecordUrlPageInfo));
    }

    @GetMapping(value = "/forceClose")
    @ResponseBody
    @Operation(summary = "强制停止推流")
    public RetResult<Void> stop(String app, String stream) {
        streamPushPlayService.stop(app, stream);
        return RetResponse.makeOKRsp();
    }

    @GetMapping(value = "/camera/meeting/list")
    @ResponseBody
    @Operation(summary = "查询会议设备")
    @Parameter(name = "topGroupAlias", description = "分组别名")
    public List<CameraChannel> queryMeetingChannelList(String topGroupAlias) {
        return channelService.queryMeetingChannelList(topGroupAlias);
    }

    @GetMapping(value = "/test")
    @ResponseBody
    public SYMember test(String device) {
        return channelService.getMember(device);
    }

}
