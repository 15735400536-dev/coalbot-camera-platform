package com.coalbot.module.camera.streamProxy.controller;

import com.alibaba.fastjson2.JSONObject;
import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.exception.ControllerException;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.service.bean.InviteErrorCode;
import com.coalbot.module.camera.streamProxy.bean.StreamProxy;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyPlayService;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyService;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.camera.vmanager.bean.StreamContent;

import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@SuppressWarnings("rawtypes")
/**
 * 拉流代理接口
 */
@Tag(name = "拉流代理", description = "")
@RestController
@Slf4j
@RequestMapping(value = "/api/proxy")
public class StreamProxyController {

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private IStreamProxyService streamProxyService;

    @Autowired
    private IStreamProxyPlayService streamProxyPlayService;

    @Autowired
    private UserSetting userSetting;


    @Operation(summary = "分页查询流代理")
    @Parameter(name = "page", description = "当前页")
    @Parameter(name = "count", description = "每页查询数量")
    @Parameter(name = "query", description = "查询内容")
    @Parameter(name = "pulling", description = "是否正在拉流")
    @Parameter(name = "mediaServerId", description = "流媒体ID")
    @GetMapping(value = "/list")
    @ResponseBody
    public PageInfo<StreamProxy> list(@RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer count,
                                      @RequestParam(required = false) String query,
                                      @RequestParam(required = false) Boolean pulling,
                                      @RequestParam(required = false) String mediaServerId) {

        if (ObjectUtils.isEmpty(mediaServerId)) {
            mediaServerId = null;
        }
        if (ObjectUtils.isEmpty(query)) {
            query = null;
        }
        return streamProxyService.getAll(page, count, query, pulling, mediaServerId);
    }

    @Operation(summary = "查询流代理")
    @Parameter(name = "app", description = "应用名")
    @Parameter(name = "stream", description = "流Id")
    @GetMapping(value = "/one")
    @ResponseBody
    public RetResult<StreamProxy> one(String app, String stream) {
        return RetResponse.makeOKRsp(streamProxyService.getStreamProxyByAppAndStream(app, stream));
    }

    @Operation(summary = "新增代理", parameters = {
            @Parameter(name = "param", description = "代理参数", required = true),
    })
    @PostMapping(value = "/add")
    @ResponseBody
    public RetResult<StreamProxy> add(@RequestBody StreamProxy param) {
        log.info("添加代理： " + JSONObject.toJSONString(param));
        if (ObjectUtils.isEmpty(param.getRelatesMediaServerId())) {
            param.setRelatesMediaServerId(null);
        }
        if (ObjectUtils.isEmpty(param.getType())) {
            param.setType("default");
        }
        if (ObjectUtils.isEmpty(param.getGbId())) {
            param.setGbDeviceId(null);
        }
        param.setServerId(userSetting.getServerId());
        streamProxyService.add(param);
        return RetResponse.makeOKRsp(param);
    }

    @Operation(summary = "更新代理", parameters = {
            @Parameter(name = "param", description = "代理参数", required = true),
    })
    @PostMapping(value = "/update")
    @ResponseBody
    public RetResult<StreamProxy> update(@RequestBody StreamProxy param) {
        log.info("更新代理： " + JSONObject.toJSONString(param));
        if (param.getId() == null) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "缺少代理信息的ID");
        }
        if (ObjectUtils.isEmpty(param.getRelatesMediaServerId())) {
            param.setRelatesMediaServerId(null);
        }
        if (ObjectUtils.isEmpty(param.getGbId())) {
            param.setGbDeviceId(null);
        }
        streamProxyService.update(param);
        return RetResponse.makeOKRsp(param);
    }

    @GetMapping(value = "/ffmpeg_cmd/list")
    @ResponseBody
    @Operation(summary = "获取ffmpeg.cmd模板")
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = true)
    public Map<String, String> getFFmpegCMDs(@RequestParam String mediaServerId) {
        log.debug("获取节点[ {} ]ffmpeg.cmd模板", mediaServerId);

        MediaServer mediaServerItem = mediaServerService.getOne(mediaServerId);
        if (mediaServerItem == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "流媒体： " + mediaServerId + "未找到");
        }
        return streamProxyService.getFFmpegCMDs(mediaServerItem);
    }

    @DeleteMapping(value = "/del")
    @ResponseBody
    @Operation(summary = "移除代理")
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流id", required = true)
    public RetResult<Void> del(@RequestParam String app, @RequestParam String stream) {
        log.info("移除代理： " + app + "/" + stream);
        if (app == null || stream == null) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), app == null ? "app不能为null" : "stream不能为null");
        } else {
            streamProxyService.delteByAppAndStream(app, stream);
        }
        return RetResponse.makeOKRsp();
    }

    @DeleteMapping(value = "/delete")
    @ResponseBody
    @Operation(summary = "移除代理")
    @Parameter(name = "id", description = "代理ID", required = true)
    public RetResult<Void> delte(String id) {
        log.info("移除代理： {}", id);
        streamProxyService.delete(id);
        return RetResponse.makeOKRsp();
    }

    @GetMapping(value = "/start")
    @ResponseBody
    @Operation(summary = "播放代理")
    @Parameter(name = "id", description = "代理Id", required = true)
    public DeferredResult<RetResult<StreamContent>> start(HttpServletRequest request, String id) {
        log.info("播放代理： {}", id);
        StreamProxy streamProxy = streamProxyService.getStreamProxy(id);
        Assert.notNull(streamProxy, "代理信息不存在");

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

        streamProxyPlayService.start(id, null, callback);
        return result;
    }

    @GetMapping(value = "/stop")
    @ResponseBody
    @Operation(summary = "停止播放")
    @Parameter(name = "id", description = "代理Id", required = true)
    public RetResult<Void> stop(String id) {
        log.info("停止播放： {}", id);
        streamProxyPlayService.stop(id);
        return RetResponse.makeOKRsp();
    }
}
