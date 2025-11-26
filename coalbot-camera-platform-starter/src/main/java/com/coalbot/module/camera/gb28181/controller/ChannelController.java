package com.coalbot.module.camera.gb28181.controller;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.dto.CommonGBChannelQueryDTO;
import com.coalbot.module.camera.gb28181.bean.*;
import com.coalbot.module.camera.gb28181.controller.bean.*;
import com.coalbot.module.camera.gb28181.service.IGbChannelPlayService;
import com.coalbot.module.camera.gb28181.service.IGbChannelService;
import com.coalbot.module.camera.gb28181.utils.VectorTileCatch;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.service.bean.InviteErrorCode;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.camera.utils.DateUtil;
import com.coalbot.module.camera.utils.TypeUtils;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.camera.vmanager.bean.StreamContent;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.PageList;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.beans.PropertyDescriptor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Tag(name = "全局通道管理")
@RestController
@Slf4j
@RequestMapping(value = "/api/common/channel")
public class ChannelController {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private IGbChannelService channelService;

    @Autowired
    private IGbChannelPlayService channelPlayService;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private VectorTileCatch vectorTileCatch;


    @Operation(summary = "查询通道信息")
    @Parameter(name = "id", description = "通道的数据库自增Id", required = true)
    @GetMapping(value = "/one")
    public RetResult<CommonGBChannel> getOne(String id) {
        return RetResponse.makeOKRsp(channelService.getOne(id));
    }

    @Operation(summary = "获取行业编码列表")
    @GetMapping("/industry/list")
    public RetResult<List<IndustryCodeType>> getIndustryCodeList() {
        return RetResponse.makeOKRsp(channelService.getIndustryCodeList());
    }

    @Operation(summary = "获取编码列表")
    @GetMapping("/type/list")
    public RetResult<List<DeviceType>> getDeviceTypeList() {
        return RetResponse.makeOKRsp(channelService.getDeviceTypeList());
    }

    @Operation(summary = "获取编码列表")
    @GetMapping("/network/identification/list")
    public RetResult<List<NetworkIdentificationType>> getNetworkIdentificationTypeList() {
        return RetResponse.makeOKRsp(channelService.getNetworkIdentificationTypeList());
    }

    @Operation(summary = "更新通道")
    @PostMapping("/update")
    public RetResult<Void> update(@RequestBody CommonGBChannel channel) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(channel);
        int count = 0;
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            String name = pd.getName();
            if ("class".equals(name)) continue;
            if (pd.getReadMethod() == null) continue;
            Object val = wrapper.getPropertyValue(name);
            if (val != null) count++;
        }
        AssertUtils.isTrue(count > 1, "未进行任何修改");
        channelService.update(channel);
        return RetResponse.makeOKRsp();
    }


    @Operation(summary = "重置国标通道")
    @PostMapping("/reset")
    public RetResult<Void> reset(@RequestBody ResetParam param) {
        AssertUtils.notNull(param.getId(), "通道ID不能为空");
        AssertUtils.notEmpty(param.getChanelFields(), "待重置字段不可以空");
        channelService.reset(param.getId(), param.getChanelFields());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "增加通道")
    @PostMapping("/add")
    public RetResult<CommonGBChannel> add(@RequestBody CommonGBChannel channel) {
        channelService.add(channel);
        return RetResponse.makeOKRsp(channel);
    }

    @Operation(summary = "获取通道列表", method = "POST")
    @PostMapping("/list")
    public RetResult<PageList<CommonGBChannel>> queryList(@RequestBody CommonGBChannelQueryDTO dto) {
        PageInfo<CommonGBChannel> pageResult = channelService.queryList(TypeUtils.longToInt(dto.getCurrent()), TypeUtils.longToInt(dto.getSize()),
                dto.getQuery(), dto.getOnline(), dto.getHasRecordPlan(), dto.getChannelType(), dto.getCivilCode(), dto.getParentDeviceId());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "获取关联行政区划通道列表", method = "POST")
    @PostMapping("/civilcode/list")
    public RetResult<PageList<CommonGBChannel>> queryListByCivilCode(@RequestBody CommonGBChannelQueryDTO dto) {
        PageInfo<CommonGBChannel> pageResult = channelService.queryListByCivilCode(TypeUtils.longToInt(dto.getCurrent()), TypeUtils.longToInt(dto.getSize()),
                dto.getQuery(), dto.getOnline(), dto.getChannelType(), dto.getCivilCode());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "存在行政区划但无法挂载的通道列表")
    @PostMapping("/civilCode/unusual/list")
    public RetResult<PageList<CommonGBChannel>> queryListByCivilCodeForUnusual(@RequestBody CommonGBChannelQueryDTO dto) {
        PageInfo<CommonGBChannel> pageResult = channelService.queryListByCivilCodeForUnusual(TypeUtils.longToInt(dto.getCurrent()), TypeUtils.longToInt(dto.getSize()),
                dto.getQuery(), dto.getOnline(), dto.getChannelType());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "存在父节点编号但无法挂载的通道列表")
    @PostMapping("/parent/unusual/list")
    public RetResult<PageList<CommonGBChannel>> queryListByParentForUnusual(@RequestBody CommonGBChannelQueryDTO dto) {
        PageInfo<CommonGBChannel> pageResult = channelService.queryListByParentForUnusual(TypeUtils.longToInt(dto.getCurrent()), TypeUtils.longToInt(dto.getSize()),
                dto.getQuery(), dto.getOnline(), dto.getChannelType());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "清除存在行政区划但无法挂载的通道列表")
    @Parameter(name = "param", description = "清理参数， all为true清理所有异常数据。 否则按照传入的设备Id清理", required = true)
    @PostMapping("/civilCode/unusual/clear")
    public RetResult<Void> clearChannelCivilCode(@RequestBody ChannelToRegionParam param) {
        channelService.clearChannelCivilCode(param.getAll(), param.getChannelIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "清除存在分组节点但无法挂载的通道列表")
    @Parameter(name = "param", description = "清理参数， all为true清理所有异常数据。 否则按照传入的设备Id清理", required = true)
    @PostMapping("/parent/unusual/clear")
    public RetResult<Void> clearChannelParent(@RequestBody ChannelToRegionParam param) {
        channelService.clearChannelParent(param.getAll(), param.getChannelIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "获取关联业务分组通道列表")
    @Parameter(name = "page", description = "当前页", required = true)
    @Parameter(name = "count", description = "每页查询数量", required = true)
    @Parameter(name = "query", description = "查询内容")
    @Parameter(name = "online", description = "是否在线")
    @Parameter(name = "channelType", description = "通道类型， 0：国标设备，1：推流设备，2：拉流代理")
    @Parameter(name = "groupDeviceId", description = "业务分组下的父节点ID")
    @GetMapping("/parent/list")
    public RetResult<PageInfo<CommonGBChannel>> queryListByParentId(int page, int count,
                                                                    @RequestParam(required = false) String query,
                                                                    @RequestParam(required = false) Boolean online,
                                                                    @RequestParam(required = false) Integer channelType,
                                                                    @RequestParam(required = false) String groupDeviceId) {
        if (ObjectUtils.isEmpty(query)) {
            query = null;
        }
        return RetResponse.makeOKRsp(channelService.queryListByParentId(page, count, query, online, channelType, groupDeviceId));
    }

    @Operation(summary = "通道设置行政区划")
    @PostMapping("/region/add")
    public RetResult<Void> addChannelToRegion(@RequestBody ChannelToRegionParam param) {
        AssertUtils.notEmpty(param.getChannelIds(), "通道ID不可为空");
        AssertUtils.notBlank(param.getCivilCode(), "未添加行政区划");
        channelService.addChannelToRegion(param.getCivilCode(), param.getChannelIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道删除行政区划")
    @PostMapping("/region/delete")
    public RetResult<Void> deleteChannelToRegion(@RequestBody ChannelToRegionParam param) {
        AssertUtils.isTrue(!param.getChannelIds().isEmpty() || !ObjectUtils.isEmpty(param.getCivilCode()), "参数异常");
        channelService.deleteChannelToRegion(param.getCivilCode(), param.getChannelIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道设置行政区划-根据国标设备")
    @PostMapping("/region/device/add")
    public RetResult<Void> addChannelToRegionByGbDevice(@RequestBody ChannelToRegionByGbDeviceParam param) {
        AssertUtils.notEmpty(param.getDeviceIds(), "参数异常");
        AssertUtils.notBlank(param.getCivilCode(), "未添加行政区划");
        channelService.addChannelToRegionByGbDevice(param.getCivilCode(), param.getDeviceIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道删除行政区划-根据国标设备")
    @PostMapping("/region/device/delete")
    public RetResult<Void> deleteChannelToRegionByGbDevice(@RequestBody ChannelToRegionByGbDeviceParam param) {
        AssertUtils.notEmpty(param.getDeviceIds(), "参数异常");
        channelService.deleteChannelToRegionByGbDevice(param.getDeviceIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道设置业务分组")
    @PostMapping("/group/add")
    public RetResult<Void> addChannelToGroup(@RequestBody ChannelToGroupParam param) {
        AssertUtils.notEmpty(param.getChannelIds(), "通道ID不可为空");
        AssertUtils.notBlank(param.getParentId(), "未添加上级分组编号");
        AssertUtils.notBlank(param.getBusinessGroup(), "未添加业务分组");
        channelService.addChannelToGroup(param.getParentId(), param.getBusinessGroup(), param.getChannelIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道删除业务分组")
    @PostMapping("/group/delete")
    public RetResult<Void> deleteChannelToGroup(@RequestBody ChannelToGroupParam param) {
        AssertUtils.isTrue(!param.getChannelIds().isEmpty()
                        || (!ObjectUtils.isEmpty(param.getParentId()) && !ObjectUtils.isEmpty(param.getBusinessGroup())),
                "参数异常");
        channelService.deleteChannelToGroup(param.getParentId(), param.getBusinessGroup(), param.getChannelIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道设置业务分组-根据国标设备")
    @PostMapping("/group/device/add")
    public RetResult<Void> addChannelToGroupByGbDevice(@RequestBody ChannelToGroupByGbDeviceParam param) {
        AssertUtils.notEmpty(param.getDeviceIds(), "参数异常");
        AssertUtils.notBlank(param.getParentId(), "未添加上级分组编号");
        AssertUtils.notBlank(param.getBusinessGroup(), "未添加业务分组");
        channelService.addChannelToGroupByGbDevice(param.getParentId(), param.getBusinessGroup(), param.getDeviceIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "通道删除业务分组-根据国标设备")
    @PostMapping("/group/device/delete")
    public RetResult<Void> deleteChannelToGroupByGbDevice(@RequestBody ChannelToGroupByGbDeviceParam param) {
        AssertUtils.notEmpty(param.getDeviceIds(), "参数异常");
        channelService.deleteChannelToGroupByGbDevice(param.getDeviceIds());
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "播放通道")
    @GetMapping("/play")
    public DeferredResult<RetResult<StreamContent>> play(HttpServletRequest request, String channelId) {
        AssertUtils.notNull(channelId, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");

        DeferredResult<RetResult<StreamContent>> result = new DeferredResult<>(userSetting.getPlayTimeout().longValue());

        ErrorCallback<StreamInfo> callback = (code, msg, streamInfo) -> {
            if (code == InviteErrorCode.SUCCESS.getCode()) {
                RetResult<StreamContent> retResult = RetResponse.makeOKRsp();
                if (streamInfo != null) {
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
                    retResult.setData(new StreamContent(streamInfo));
                } else {
                    retResult.setCode(code);
                    retResult.setMsg(msg);
                }
                result.setResult(retResult);
            } else {
                result.setResult(RetResponse.makeRsp(code, msg));
            }
        };
        channelPlayService.play(channel, null, userSetting.getRecordSip(), callback);
        return result;
    }

    @Operation(summary = "停止播放通道")
    @GetMapping("/play/stop")
    public RetResult<Void> stopPlay(String channelId) {
        AssertUtils.notNull(channelId, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");
        channelPlayService.stopPlay(channel);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "录像查询")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @GetMapping("/playback/query")
    public DeferredResult<RetResult<List<CommonRecordInfo>>> queryRecord(String channelId, String startTime, String endTime) {

        DeferredResult<RetResult<List<CommonRecordInfo>>> result = new DeferredResult<>(Long.valueOf(userSetting.getRecordInfoTimeout()), TimeUnit.MILLISECONDS);
        if (!DateUtil.verification(startTime, DateUtil.formatter)) {
            throw new CommonException("startTime格式为" + DateUtil.PATTERN);
        }
        if (!DateUtil.verification(endTime, DateUtil.formatter)) {
            throw new CommonException("endTime格式为" + DateUtil.PATTERN);
        }
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");

        channelPlayService.queryRecord(channel, startTime, endTime, (code, msg, data) -> {
            RetResult<List<CommonRecordInfo>> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        });
        result.onTimeout(() -> {
            RetResult<List<CommonRecordInfo>> RetResult = new RetResult<>();
            RetResult.setCode(ErrorCode.ERROR100.getCode());
            RetResult.setMsg("timeout");
            result.setResult(RetResult);
        });
        return result;
    }

    @Operation(summary = "录像回放")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @GetMapping("/playback")
    public DeferredResult<RetResult<StreamContent>> playback(HttpServletRequest request, String channelId, String startTime, String endTime) {
        AssertUtils.notNull(channelId, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");

        DeferredResult<RetResult<StreamContent>> result = new DeferredResult<>(userSetting.getPlayTimeout().longValue());

        ErrorCallback<StreamInfo> callback = (code, msg, streamInfo) -> {
            if (code == InviteErrorCode.SUCCESS.getCode()) {
                RetResult<StreamContent> RetResult = RetResponse.makeOKRsp();
                if (streamInfo != null) {
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
                    RetResult.setData(new StreamContent(streamInfo));
                } else {
                    RetResult.setCode(code);
                    RetResult.setMsg(msg);
                }

                result.setResult(RetResult);
            } else {
                result.setResult(RetResponse.makeRsp(code, msg));
            }
        };
        channelPlayService.playback(channel, DateUtil.yyyy_MM_dd_HH_mm_ssToTimestamp(startTime),
                DateUtil.yyyy_MM_dd_HH_mm_ssToTimestamp(endTime), callback);
        return result;
    }

    @Operation(summary = "停止录像回放")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @GetMapping("/playback/stop")
    public RetResult<Void> stopPlayback(String channelId, String stream) {
        AssertUtils.notNull(channelId, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");
        channelPlayService.stopPlayback(channel, stream);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "暂停录像回放")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @GetMapping("/playback/pause")
    public RetResult<Void> pausePlayback(String channelId, String stream) {
        AssertUtils.notNull(channelId, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");
        channelPlayService.playbackPause(channel, stream);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "恢复录像回放")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @GetMapping("/playback/resume")
    public RetResult<Void> resumePlayback(String channelId, String stream) {
        AssertUtils.notNull(channelId, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");
        channelPlayService.playbackResume(channel, stream);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "拖动录像回放")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @Parameter(name = "seekTime", description = "将要播放的时间", required = true)
    @GetMapping("/playback/seek")
    public RetResult<Void> seekPlayback(String channelId, String stream, Long seekTime) {
        AssertUtils.notNull(channelId, "参数异常");
        AssertUtils.notNull(seekTime, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");
        channelPlayService.playbackSeek(channel, stream, seekTime);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "拖动录像回放")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @Parameter(name = "speed", description = "倍速", required = true)
    @GetMapping("/playback/speed")
    public RetResult<Void> seekPlayback(String channelId, String stream, Double speed) {
        AssertUtils.notNull(channelId, "参数异常");
        AssertUtils.notNull(speed, "参数异常");
        CommonGBChannel channel = channelService.getOne(channelId);
        AssertUtils.notNull(channel, "通道不存在");
        channelPlayService.playbackSpeed(channel, stream, speed);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "为地图获取通道列表")
    @Parameter(name = "query", description = "查询内容")
    @Parameter(name = "online", description = "是否在线")
    @Parameter(name = "hasRecordPlan", description = "是否已设置录制计划")
    @Parameter(name = "channelType", description = "通道类型， 0：国标设备，1：推流设备，2：拉流代理")
    @Parameter(name = "geoCoordSys", description = "地理坐标系， WGS84/GCJ02")
    @GetMapping("/map/list")
    public RetResult<List<CommonGBChannel>> queryListForMap(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean online,
            @RequestParam(required = false) Boolean hasRecordPlan,
            @RequestParam(required = false) Integer channelType) {
        if (ObjectUtils.isEmpty(query)) {
            query = null;
        }
        return RetResponse.makeOKRsp(channelService.queryListForMap(query, online, hasRecordPlan, channelType));
    }

    @Operation(summary = "为地图去除抽稀结果")
    @PostMapping("/map/reset-level")
    public RetResult<Void> resetLevel() {
        channelService.resetLevel();
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "执行抽稀")
    @PostMapping("/map/thin/draw")
    public RetResult<String> drawThin(@RequestBody DrawThinParam param) {
        if (param == null || param.getZoomParam() == null || param.getZoomParam().isEmpty()) {
            throw new CommonException(ErrorCode.ERROR400.getMsg());
        }
        return RetResponse.makeOKRsp(channelService.drawThin(param.getZoomParam(), param.getExtent(), param.getGeoCoordSys()));
    }

    @Operation(summary = "清除未保存的抽稀结果")
    @Parameter(name = "id", description = "抽稀ID", required = true)
    @GetMapping("/map/thin/clear")
    public RetResult<Void> clearThin(String id) {
        vectorTileCatch.remove(id);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "保存的抽稀结果")
    @Parameter(name = "id", description = "抽稀ID", required = true)
    @GetMapping("/map/thin/save")
    public RetResult<Void> saveThin(String id) {
        channelService.saveThin(id);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "获取抽稀执行的进度")
    @Parameter(name = "id", description = "抽稀ID", required = true)
    @GetMapping("/map/thin/progress")
    public DrawThinProcess thinProgress(String id) {
        return channelService.thinProgress(id);
    }

    @Operation(summary = "为地图提供标准mvt图层")
    @GetMapping(value = "/map/tile/{z}/{x}/{y}", produces = "application/x-protobuf")
    @Parameter(name = "geoCoordSys", description = "地理坐标系， WGS84/GCJ02")
    public ResponseEntity<byte[]> getTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, String geoCoordSys) {

        try {
            byte[] mvt = channelService.getTile(z, x, y, geoCoordSys);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/x-protobuf"));
            if (mvt == null) {
                headers.setContentLength(0);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            headers.setContentLength(mvt.length);
            return new ResponseEntity<>(mvt, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("构建矢量瓦片失败： z: {}, x: {}, y:{}", z, x, y, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "为地图提供经过抽稀的标准mvt图层")
    @GetMapping(value = "/map/thin/tile/{z}/{x}/{y}", produces = "application/x-protobuf")
    @Parameter(name = "geoCoordSys", description = "地理坐标系， WGS84/GCJ02")
    @Parameter(name = "thinId", description = "抽稀结果ID")
    public ResponseEntity<byte[]> getThinTile(@PathVariable int z, @PathVariable int x, @PathVariable int y,
                                              String geoCoordSys, @RequestParam(required = false) String thinId) {

        if (ObjectUtils.isEmpty(thinId)) {
            thinId = "DEFAULT";
        }
        String catchKey = z + "_" + x + "_" + y + "_" + geoCoordSys.toUpperCase();
        byte[] mvt = vectorTileCatch.getVectorTile(thinId, catchKey);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-protobuf"));
        if (mvt == null) {
            headers.setContentLength(0);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }

        headers.setContentLength(mvt.length);
        return new ResponseEntity<>(mvt, headers, HttpStatus.OK);
    }


}
