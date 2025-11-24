package com.coalbot.module.camera.streamPush.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.coalbot.module.camera.common.enums.ChannelDataType;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.exception.ControllerException;
import com.coalbot.module.camera.gb28181.transmit.callback.DeferredResultHolder;
import com.coalbot.module.camera.gb28181.transmit.callback.RequestMessage;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.service.IMediaService;
import com.coalbot.module.camera.streamPush.bean.BatchRemoveParam;
import com.coalbot.module.camera.streamPush.bean.StreamPush;
import com.coalbot.module.camera.streamPush.bean.StreamPushExcelDto;
import com.coalbot.module.camera.streamPush.enent.StreamPushUploadFileHandler;
import com.coalbot.module.camera.streamPush.service.IStreamPushPlayService;
import com.coalbot.module.camera.streamPush.service.IStreamPushService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "推流信息管理")
@RestController
@Slf4j
@RequestMapping(value = "/api/push")
public class StreamPushController {

    @Autowired
    private IStreamPushService streamPushService;

    @Autowired
    private IStreamPushPlayService streamPushPlayService;

    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private DeferredResultHolder resultHolder;

    @Autowired
    private IMediaService mediaService;

    @Autowired
    private UserSetting userSetting;

    @GetMapping(value = "/list")
    @ResponseBody
    @Operation(summary = "推流列表查询")
    @Parameter(name = "page", description = "当前页")
    @Parameter(name = "count", description = "每页查询数量")
    @Parameter(name = "query", description = "查询内容")
    @Parameter(name = "pushing", description = "是否正在推流")
    @Parameter(name = "mediaServerId", description = "流媒体ID")
    public RetResult<PageInfo<StreamPush>> list(@RequestParam(required = false) Integer page,
                                                @RequestParam(required = false) Integer count,
                                                @RequestParam(required = false) String query,
                                                @RequestParam(required = false) Boolean pushing,
                                                @RequestParam(required = false) String mediaServerId) {

        if (ObjectUtils.isEmpty(query)) {
            query = null;
        }
        if (ObjectUtils.isEmpty(mediaServerId)) {
            mediaServerId = null;
        }
        PageInfo<StreamPush> pushList = streamPushService.getPushList(page, count, query, pushing, mediaServerId);
        return RetResponse.makeOKRsp(pushList);
    }


    @PostMapping(value = "/remove")
    @ResponseBody
    @Operation(summary = "删除")
    @Parameter(name = "id", description = "应用名", required = true)
    public RetResult<Void> delete(String id) {
        if (streamPushService.delete(id) <= 0) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
        return RetResponse.makeOKRsp();
    }

    @PostMapping(value = "upload")
    @ResponseBody
    public DeferredResult<ResponseEntity<RetResult<Object>>> uploadChannelFile(@RequestParam(value = "file") MultipartFile file) {

        // 最多处理文件一个小时
        DeferredResult<ResponseEntity<RetResult<Object>>> result = new DeferredResult<>(60 * 60 * 1000L);
        // 录像查询以channelId作为deviceId查询
        String key = DeferredResultHolder.UPLOAD_FILE_CHANNEL;
        String uuid = UUID.randomUUID().toString();
        log.info("通道导入文件类型: {}", file.getContentType());
        if (file.isEmpty()) {
            log.warn("通道导入文件为空");
            RetResult<Object> RetResult = new RetResult<>();
            RetResult.setCode(-1);
            RetResult.setMsg("文件为空");
            result.setResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RetResult));
            return result;
        }
        if (file.getContentType() == null) {
            RetResult<Object> RetResult = new RetResult<>();
            RetResult.setCode(-1);
            RetResult.setMsg("无法识别文件类型");
            result.setResult(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RetResult));
            return result;
        }
        // 同时只处理一个文件
        if (resultHolder.exist(key, null)) {
            log.warn("已有导入任务正在执行");
            RetResult<Object> RetResult = new RetResult<>();
            RetResult.setCode(-1);
            RetResult.setMsg("已有导入任务正在执行");
            result.setResult(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(RetResult));
            return result;
        }

        resultHolder.put(key, uuid, result);
        result.onTimeout(() -> {
            log.warn("通道导入超时，可能文件过大");
            RequestMessage msg = new RequestMessage();
            msg.setKey(key);
            RetResult<Object> RetResult = new RetResult<>();
            RetResult.setCode(-1);
            RetResult.setMsg("导入超时，可能文件过大");
            msg.setData(RetResult);
            resultHolder.invokeAllResult(msg);
        });
        //获取文件流
        InputStream inputStream = null;
        try {
            String name = file.getName();
            inputStream = file.getInputStream();
        } catch (IOException e) {
            log.error("未处理的异常 ", e);
        }
        try {
            //传入参数
            ExcelReader excelReader = EasyExcel.read(inputStream, StreamPushExcelDto.class,
                    new StreamPushUploadFileHandler(streamPushService, mediaServerService.getDefaultMediaServer().getId(), (errorStreams, errorGBs) -> {
                        log.info("通道导入成功，存在重复App+Stream为{}个，存在国标ID为{}个", errorStreams.size(), errorGBs.size());
                        RequestMessage msg = new RequestMessage();
                        msg.setKey(key);
                        RetResult<Map<String, List<String>>> RetResult = new RetResult<>();
                        if (errorStreams.isEmpty() && errorGBs.isEmpty()) {
                            RetResult.setCode(0);
                            RetResult.setMsg("成功");
                        } else {
                            RetResult.setCode(1);
                            RetResult.setMsg("导入成功。但是存在重复数据");
                            Map<String, List<String>> errorData = new HashMap<>();
                            errorData.put("gbId", errorGBs);
                            errorData.put("stream", errorStreams);
                            RetResult.setData(errorData);
                        }
                        msg.setData(RetResult);
                        resultHolder.invokeAllResult(msg);
                    })).build();
            ReadSheet readSheet = EasyExcel.readSheet(0).build();
            excelReader.read(readSheet);
            excelReader.finish();
        } catch (ExcelDataConvertException e) {
            log.error("通道导入失败：行： {}， 列： {}, 内容： {}", e.getRowIndex(), e.getColumnIndex(), e.getCellData().getStringValue());
            RequestMessage msg = new RequestMessage();
            msg.setKey(key);
            RetResult<Object> RetResult = new RetResult<>();
            RetResult.setCode(ErrorCode.ERROR100.getCode());
            RetResult.setMsg("数据异常: " + e.getRowIndex() + "行" + e.getColumnIndex() + "列, 内容：" + e.getCellData().getStringValue());
            msg.setData(RetResult);
            resultHolder.invokeAllResult(msg);
        } catch (Exception e) {
            log.warn("通道导入失败：", e);
            RequestMessage msg = new RequestMessage();
            msg.setKey(key);
            RetResult<Object> RetResult = new RetResult<>();
            RetResult.setCode(ErrorCode.ERROR100.getCode());
            RetResult.setMsg("通道导入失败: " + e.getMessage());
            msg.setData(RetResult);
            resultHolder.invokeAllResult(msg);
        }

        return result;
    }

    /**
     * 添加推流信息
     *
     * @param stream 推流信息
     * @return
     */
    @PostMapping(value = "/add")
    @ResponseBody
    @Operation(summary = "添加推流信息")
    public RetResult<StreamPush> add(@RequestBody StreamPush stream) {
        if (ObjectUtils.isEmpty(stream.getGbId())) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "国标ID不可为空");
        }
        if (ObjectUtils.isEmpty(stream.getApp()) && ObjectUtils.isEmpty(stream.getStream())) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "app或stream不可为空");
        }
        stream.setGbStatus("OFF");
        stream.setPushing(false);
        if (!streamPushService.add(stream)) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
        stream.setDataType(ChannelDataType.STREAM_PUSH);
        stream.setDataDeviceId(stream.getId());
        return RetResponse.makeOKRsp(stream);
    }

    @PostMapping(value = "/update")
    @ResponseBody
    @Operation(summary = "更新推流信息")
    public RetResult<Void> update(@RequestBody StreamPush stream) {
        if (ObjectUtils.isEmpty(stream.getId())) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "ID不可为空");
        }
        if (!streamPushService.update(stream)) {
            throw new ControllerException(ErrorCode.ERROR100);
        }
        return RetResponse.makeOKRsp();
    }

    @DeleteMapping(value = "/batchRemove")
    @ResponseBody
    @Operation(summary = "删除多个推流")
    public RetResult<Void> batchStop(@RequestBody BatchRemoveParam ids) {
        if (ids.getIds().isEmpty()) {
            return RetResponse.makeOKRsp();
        }
        streamPushService.batchRemove(ids.getIds());
        return RetResponse.makeOKRsp();
    }

    @GetMapping(value = "/start")
    @ResponseBody
    @Operation(summary = "开始播放")
    public DeferredResult<RetResult<StreamContent>> start(HttpServletRequest request, String id) {
        Assert.notNull(id, "推流ID不可为NULL");
        DeferredResult<RetResult<StreamContent>> result = new DeferredResult<>(userSetting.getPlayTimeout().longValue());
        result.onTimeout(() -> {
            RetResult<StreamContent> fail = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "等待推流超时");
            result.setResult(fail);
        });
        streamPushPlayService.start(id, (code, msg, streamInfo) -> {
            if (code == 0 && streamInfo != null) {
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
                RetResult<StreamContent> success = RetResponse.makeOKRsp(new StreamContent(streamInfo));
                result.setResult(success);
            }
        }, null, null);
        return result;
    }

    @GetMapping(value = "/forceClose")
    @ResponseBody
    @Operation(summary = "强制停止推流")
    public RetResult<Void> stop(String app, String stream) {
        streamPushPlayService.stop(app, stream);
        return RetResponse.makeOKRsp();
    }
}
