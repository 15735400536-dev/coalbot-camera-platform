package com.coalbot.module.camera.gb28181.controller;

import com.coalbot.module.camera.common.StreamInfo;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.media.zlm.dto.StreamAuthorityInfo;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.service.bean.InviteErrorCode;
import com.coalbot.module.camera.storager.IRedisCatchStorage;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyService;
import com.coalbot.module.camera.vmanager.bean.StreamContent;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;


@Tag(name  = "媒体流相关")
@RestController
@Slf4j
@RequestMapping(value = "/api/media")
public class MediaController {

    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private IStreamProxyService streamProxyService;

    @Autowired
    private IMediaServerService mediaServerService;


    /**
     * 根据应用名和流id获取播放地址
     * @param app 应用名
     * @param stream 流id
     * @return
     */
    @Operation(summary = "根据应用名和流id获取播放地址")
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流id", required = true)
    @Parameter(name = "mediaServerId", description = "媒体服务器id")
    @Parameter(name = "callId", description = "推流时携带的自定义鉴权ID")
    @Parameter(name = "useSourceIpAsStreamIp", description = "是否使用请求IP作为返回的地址IP")
    @GetMapping(value = "/stream_info_by_app_and_stream")
    @ResponseBody
    public DeferredResult<RetResult<StreamContent>> getStreamInfoByAppAndStream(HttpServletRequest request, @RequestParam String app,
                                                                                @RequestParam String stream,
                                                                                @RequestParam(required = false) String mediaServerId,
                                                                                @RequestParam(required = false) String callId,
                                                                                @RequestParam(required = false) Boolean useSourceIpAsStreamIp){
        DeferredResult<RetResult<StreamContent>> result = new DeferredResult<>();
        boolean authority = false;
        if (callId != null) {
            // 权限校验
            StreamAuthorityInfo streamAuthorityInfo = redisCatchStorage.getStreamAuthorityInfo(app, stream);
            if (streamAuthorityInfo != null
                    && streamAuthorityInfo.getCallId() != null
                    && streamAuthorityInfo.getCallId().equals(callId)) {
                authority = true;
            }else {
                throw new CommonException("获取播放地址鉴权失败");
            }
        }else {
            // 是否登陆用户, 登陆用户返回完整信息
//            LoginUser userInfo = SecurityUtils.getUserInfo();
//            if (userInfo!= null) {
//                authority = true;
//            }
            authority = true;
        }
        StreamInfo streamInfo;
        if (useSourceIpAsStreamIp != null && useSourceIpAsStreamIp) {
            String host = request.getHeader("Host");
            String localAddr = host.split(":")[0];
            log.info("使用{}作为返回流的ip", localAddr);
            streamInfo = mediaServerService.getStreamInfoByAppAndStreamWithCheck(app, stream, mediaServerId, localAddr, authority);
        }else {
            streamInfo = mediaServerService.getStreamInfoByAppAndStreamWithCheck(app, stream, mediaServerId, authority);
        }

        if (streamInfo != null){
//            RetResult<StreamContent> RetResult = RetResponse.makeOKRsp();
//            RetResult.setData(new StreamContent(streamInfo));
            result.setResult(RetResponse.makeOKRsp(new StreamContent(streamInfo)));
        }else {
            ErrorCallback<StreamInfo> callback = (code, msg, streamInfoStoStart) -> {
                if (code == InviteErrorCode.SUCCESS.getCode()) {
//                    RetResult<StreamContent> RetResult = RetResponse.makeOKRsp();
                    if (useSourceIpAsStreamIp != null && useSourceIpAsStreamIp) {
                        String host;
                        try {
                            URL url=new URL(request.getRequestURL().toString());
                            host=url.getHost();
                        } catch (MalformedURLException e) {
                            host=request.getLocalAddr();
                        }
                        streamInfoStoStart.changeStreamIp(host);
                    }
                    if (!ObjectUtils.isEmpty(streamInfoStoStart.getMediaServer().getTranscodeSuffix())
                            && !"null".equalsIgnoreCase(streamInfoStoStart.getMediaServer().getTranscodeSuffix())) {
                        streamInfoStoStart.setStream(streamInfoStoStart.getStream() + "_" + streamInfoStoStart.getMediaServer().getTranscodeSuffix());
                    }
//                    RetResult.setData(new StreamContent(streamInfoStoStart));
                    result.setResult(RetResponse.makeOKRsp(new StreamContent(streamInfoStoStart)));
                }else {
                    result.setResult(RetResponse.makeRsp(code, msg));
                }
            };
            //获取流失败，重启拉流后重试一次
            streamProxyService.startByAppAndStream(app, stream, callback);
        }
        return result;
    }
    /**
     * 获取推流播放地址
     * @param app 应用名
     * @param stream 流id
     * @return
     */
    @GetMapping(value = "/getPlayUrl")
    @ResponseBody
    @Operation(summary = "获取推流播放地址")
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流id", required = true)
    @Parameter(name = "mediaServerId", description = "媒体服务器id")
    public RetResult<StreamContent> getPlayUrl(@RequestParam String app, @RequestParam String stream,
                                    @RequestParam(required = false) String mediaServerId){
        boolean authority = false;
        // 是否登陆用户, 登陆用户返回完整信息
//        LoginUser userInfo = SecurityUtils.getUserInfo();
//        if (userInfo!= null) {
//            authority = true;
//        }
        authority = true;
        StreamInfo streamInfo = mediaServerService.getStreamInfoByAppAndStreamWithCheck(app, stream, mediaServerId, authority);
        if (streamInfo == null){
            throw new CommonException("获取播放地址失败");
        }
        return RetResponse.makeOKRsp(new StreamContent(streamInfo));
    }
}
