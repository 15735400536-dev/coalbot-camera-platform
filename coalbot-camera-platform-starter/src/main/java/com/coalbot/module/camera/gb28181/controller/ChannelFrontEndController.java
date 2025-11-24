package com.coalbot.module.camera.gb28181.controller;

import com.coalbot.module.camera.conf.exception.ControllerException;
import com.coalbot.module.camera.gb28181.bean.*;
import com.coalbot.module.camera.gb28181.service.IGbChannelControlService;
import com.coalbot.module.camera.gb28181.service.IGbChannelService;
import com.coalbot.module.camera.service.bean.ErrorCallback;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;

import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;


@Tag(name  = "全局通道前端控制")
@RestController
@Slf4j
@RequestMapping(value = "/api/common/channel/front-end")
public class ChannelFrontEndController {

    @Autowired
    private IGbChannelService channelService;

    @Autowired
    private IGbChannelControlService channelControlService;


    @Operation(summary = "云台控制")
    @Parameter(name = "channelId", description = "通道ID", required = true)
    @Parameter(name = "command", description = "控制指令,允许值: left, right, up, down, upleft, upright, downleft, downright, zoomin, zoomout, stop", required = true)
    @Parameter(name = "panSpeed", description = "水平速度(0-100)", required = true)
    @Parameter(name = "tiltSpeed", description = "垂直速度(0-100)", required = true)
    @Parameter(name = "zoomSpeed", description = "缩放速度(0-100)", required = true)
    @GetMapping("/ptz")
    public DeferredResult<RetResult<String>> ptz(String channelId, String command, Integer panSpeed, Integer tiltSpeed, Integer zoomSpeed){

        if (log.isDebugEnabled()) {
            log.debug("[通用通道]云台控制 API调用，channelId：{} ，command：{} ，panSpeed：{} ，tiltSpeed：{} ，zoomSpeed：{}",channelId, command, panSpeed, tiltSpeed, zoomSpeed);
        }

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        if (panSpeed == null) {
            panSpeed = 50;
        }else if (panSpeed < 0 || panSpeed > 100) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "panSpeed 为 0-100的数字");
        }
        if (tiltSpeed == null) {
            tiltSpeed = 50;
        }else if (tiltSpeed < 0 || tiltSpeed > 100) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "tiltSpeed 为 0-100的数字");
        }
        if (zoomSpeed == null) {
            zoomSpeed = 50;
        }else if (zoomSpeed < 0 || zoomSpeed > 100) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "zoomSpeed 为 0-100的数字");
        }

        FrontEndControlCodeForPTZ controlCode = new FrontEndControlCodeForPTZ();
        controlCode.setPanSpeed(panSpeed);
        controlCode.setTiltSpeed(tiltSpeed);
        controlCode.setZoomSpeed(zoomSpeed);
        switch (command){
            case "left":
                controlCode.setPan(0);
                break;
            case "right":
                controlCode.setPan(1);
                break;
            case "up":
                controlCode.setTilt(0);
                break;
            case "down":
                controlCode.setTilt(1);
                break;
            case "upleft":
                controlCode.setPan(0);
                controlCode.setTilt(0);
                break;
            case "upright":
                controlCode.setTilt(0);
                controlCode.setPan(1);
                break;
            case "downleft":
                controlCode.setPan(0);
                controlCode.setTilt(1);
                break;
            case "downright":
                controlCode.setTilt(1);
                controlCode.setPan(1);
                break;
            case "zoomin":
                controlCode.setZoom(1);
                break;
            case "zoomout":
                controlCode.setZoom(0);
                break;
            default:
                break;
        }

        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        channelControlService.ptz(channel, controlCode, (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        });
        return result;
    }


    @Operation(summary = "光圈控制")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "command", description = "控制指令,允许值: in, out, stop", required = true)
    @Parameter(name = "speed", description = "光圈速度(0-100)", required = true)
    @GetMapping("/fi/iris")
    public DeferredResult<RetResult<String>> iris(String channelId, String command, Integer speed){

        if (log.isDebugEnabled()) {
            log.debug("[通用通道]光圈控制 API调用，channelId：{} ，command：{} ，speed：{} ",channelId, command, speed);
        }

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        if (speed == null) {
            speed = 50;
        }else if (speed < 0 || speed > 100) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "speed 为 0-100的数字");
        }

        FrontEndControlCodeForFI controlCode = new FrontEndControlCodeForFI();
        controlCode.setIrisSpeed(speed);

        switch (command){
            case "in":
                controlCode.setIris(1);
                break;
            case "out":
                controlCode.setIris(0);
                break;
            default:
                break;
        }

        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };

        channelControlService.fi(channel, controlCode, callback);

        return result;
    }

    @Operation(summary = "聚焦控制")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "command", description = "控制指令,允许值: near, far, stop", required = true)
    @Parameter(name = "speed", description = "聚焦速度(0-100)", required = true)
    @GetMapping("/fi/focus")
    public DeferredResult<RetResult<String>> focus(String channelId, String command, Integer speed){

        if (log.isDebugEnabled()) {
            log.debug("[通用通道]聚焦控制 API调用，channelId：{} ，command：{} ，speed：{} ", channelId, command, speed);
        }

        if (speed == null) {
            speed = 50;
        }else if (speed < 0 || speed > 100) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "speed 为 0-100的数字");
        }

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        FrontEndControlCodeForFI controlCode = new FrontEndControlCodeForFI();
        controlCode.setFocusSpeed(speed);
        switch (command){
            case "near":
                controlCode.setFocus(0);
                break;
            case "far":
                controlCode.setFocus(1);
                break;
            default:
                break;
        }

        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };

        channelControlService.fi(channel, controlCode, callback);
        return result;
    }

    @Operation(summary = "查询预置位")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @GetMapping("/preset/query")
    public DeferredResult<RetResult<List<Preset>>> queryPreset(String channelId) {
        if (log.isDebugEnabled()) {
            log.debug("[通用通道] 预置位查询API调用, {}", channelId);
        }

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        DeferredResult<RetResult<List<Preset>>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<List<Preset>> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<List<Preset>> callback = (code, msg, data) -> {
            RetResult<List<Preset>> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };

        channelControlService.queryPreset(channel, callback);

        return result;
    }

    private DeferredResult<RetResult<String>> controlPreset(String channelId, FrontEndControlCodeForPreset controlCode) {
        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");


        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };

        channelControlService.preset(channel, controlCode, callback);
        return result;
    }

    @Operation(summary = "预置位指令-设置预置位")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "presetId", description = "预置位编号", required = true)
    @Parameter(name = "presetName", description = "预置位名称", required = true)
    @GetMapping("/preset/add")
    public DeferredResult<RetResult<String>> addPreset(String channelId, Integer presetId, String presetName) {
        FrontEndControlCodeForPreset controlCode = new FrontEndControlCodeForPreset();
        controlCode.setCode(1);
        controlCode.setPresetId(presetId);
        controlCode.setPresetName(presetName);

        return controlPreset(channelId, controlCode);
    }

    @Operation(summary = "预置位指令-调用预置位")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "presetId", description = "预置位编号(1-100)", required = true)
    @GetMapping("/preset/call")
    public DeferredResult<RetResult<String>> callPreset(String channelId, Integer presetId) {
        FrontEndControlCodeForPreset controlCode = new FrontEndControlCodeForPreset();
        controlCode.setCode(2);
        controlCode.setPresetId(presetId);

        return controlPreset(channelId, controlCode);
    }

    @Operation(summary = "预置位指令-删除预置位")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "presetId", description = "预置位编号(1-100)", required = true)
    @GetMapping("/preset/delete")
    public DeferredResult<RetResult<String>> deletePreset(String channelId, Integer presetId) {

        FrontEndControlCodeForPreset controlCode = new FrontEndControlCodeForPreset();
        controlCode.setCode(3);
        controlCode.setPresetId(presetId);

        return controlPreset(channelId, controlCode);
    }

    private DeferredResult<RetResult<String>> tourControl(String channelId, FrontEndControlCodeForTour controlCode) {
        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };

        channelControlService.tour(channel, controlCode, callback);
        return result;
    }

    @Operation(summary = "巡航指令-加入巡航点")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "tourId", description = "巡航组号", required = true)
    @Parameter(name = "presetId", description = "预置位编号", required = true)
    @GetMapping("/tour/point/add")
    public DeferredResult<RetResult<String>> addTourPoint(String channelId, Integer tourId, Integer presetId) {

        FrontEndControlCodeForTour controlCode = new FrontEndControlCodeForTour();
        controlCode.setCode(1);
        controlCode.setPresetId(presetId);
        controlCode.setTourId(tourId);

        return tourControl(channelId, controlCode);
    }

    @Operation(summary = "巡航指令-删除一个巡航点")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "tourId", description = "巡航组号(1-100)", required = true)
    @Parameter(name = "presetId", description = "预置位编号(0-100, 为0时删除整个巡航)", required = true)
    @GetMapping("/tour/point/delete")
    public DeferredResult<RetResult<String>> deleteCruisePoint(String channelId, Integer tourId, Integer presetId) {
        FrontEndControlCodeForTour controlCode = new FrontEndControlCodeForTour();
        controlCode.setCode(2);
        controlCode.setPresetId(presetId);
        controlCode.setTourId(tourId);

        return tourControl(channelId, controlCode);
    }

    @Operation(summary = "巡航指令-设置巡航速度")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "tourId", description = "巡航组号(0-100)", required = true)
    @Parameter(name = "speed", description = "巡航速度(1-4095)", required = true)
    @Parameter(name = "presetId", description = "预置位编号", required = true)
    @GetMapping("/tour/speed")
    public DeferredResult<RetResult<String>> setCruiseSpeed(String channelId, Integer tourId, Integer speed, Integer presetId) {
        FrontEndControlCodeForTour controlCode = new FrontEndControlCodeForTour();
        controlCode.setCode(3);
        controlCode.setTourSpeed(speed);
        controlCode.setTourId(tourId);
        controlCode.setPresetId(presetId);
        return tourControl(channelId, controlCode);
    }

    @Operation(summary = "巡航指令-设置巡航停留时间")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "tourId", description = "巡航组号", required = true)
    @Parameter(name = "time", description = "巡航停留时间(1-4095)", required = true)
    @Parameter(name = "presetId", description = "预置位编号", required = true)
    @GetMapping("/tour/time")
    public DeferredResult<RetResult<String>> setCruiseTime(String channelId, Integer tourId, Integer time, Integer presetId) {
        FrontEndControlCodeForTour controlCode = new FrontEndControlCodeForTour();
        controlCode.setCode(4);
        controlCode.setTourTime(time);
        controlCode.setTourId(tourId);
        controlCode.setPresetId(presetId);
        return tourControl(channelId, controlCode);
    }

    @Operation(summary = "巡航指令-开始巡航")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "tourId", description = "巡航组号)", required = true)
    @GetMapping("/tour/start")
    public DeferredResult<RetResult<String>> startCruise(String channelId, Integer tourId) {
        FrontEndControlCodeForTour controlCode = new FrontEndControlCodeForTour();
        controlCode.setCode(5);
        controlCode.setTourId(tourId);
        return tourControl(channelId, controlCode);
    }

    @Operation(summary = "巡航指令-停止巡航")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "tourId", description = "巡航组号", required = true)
    @GetMapping("/tour/stop")
    public DeferredResult<RetResult<String>> stopCruise(String channelId, Integer tourId) {
        FrontEndControlCodeForTour controlCode = new FrontEndControlCodeForTour();
        controlCode.setCode(6);
        controlCode.setTourId(tourId);
        return tourControl(channelId, controlCode);
    }

    private DeferredResult<RetResult<String>> scanControl(String channelId, FrontEndControlCodeForScan controlCode) {

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");
        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };
        channelControlService.scan(channel, controlCode, callback);

        return result;

    }

    @Operation(summary = "扫描指令-开始自动扫描")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "scanId", description = "扫描组号(0-100)", required = true)
    @GetMapping("/scan/start")
    public DeferredResult<RetResult<String>> startScan(String channelId, Integer scanId) {
        FrontEndControlCodeForScan controlCode = new FrontEndControlCodeForScan();
        controlCode.setCode(1);
        controlCode.setScanId(scanId);
        return scanControl(channelId, controlCode);

    }

    @Operation(summary = "扫描指令-停止自动扫描")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "scanId", description = "扫描组号(0-100)", required = true)
    @GetMapping("/scan/stop")
    public DeferredResult<RetResult<String>> stopScan(String channelId, Integer scanId) {
        FrontEndControlCodeForScan controlCode = new FrontEndControlCodeForScan();
        controlCode.setCode(5);
        controlCode.setScanId(scanId);
        return scanControl(channelId, controlCode);
    }

    @Operation(summary = "扫描指令-设置自动扫描左边界")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "scanId", description = "扫描组号(0-100)", required = true)
    @GetMapping("/scan/set/left")
    public DeferredResult<RetResult<String>> setScanLeft(String channelId, Integer scanId) {
        FrontEndControlCodeForScan controlCode = new FrontEndControlCodeForScan();
        controlCode.setCode(2);
        controlCode.setScanId(scanId);
        return scanControl(channelId, controlCode);
    }

    @Operation(summary = "扫描指令-设置自动扫描右边界")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "scanId", description = "扫描组号(0-100)", required = true)
    @GetMapping("/scan/set/right")
    public DeferredResult<RetResult<String>> setScanRight(String channelId, Integer scanId) {
        FrontEndControlCodeForScan controlCode = new FrontEndControlCodeForScan();
        controlCode.setCode(3);
        controlCode.setScanId(scanId);
        return scanControl(channelId, controlCode);
    }


    @Operation(summary = "扫描指令-设置自动扫描速度")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "scanId", description = "扫描组号(0-100)", required = true)
    @Parameter(name = "speed", description = "自动扫描速度(1-4095)", required = true)
    @GetMapping("/scan/set/speed")
    public DeferredResult<RetResult<String>> setScanSpeed(String channelId, Integer scanId, Integer speed) {
        FrontEndControlCodeForScan controlCode = new FrontEndControlCodeForScan();
        controlCode.setCode(4);
        controlCode.setScanId(scanId);
        controlCode.setScanSpeed(speed);
        return scanControl(channelId, controlCode);
    }


    @Operation(summary = "辅助开关控制指令-雨刷控制")
    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "command", description = "控制指令,允许值: on, off", required = true)
    @GetMapping("/wiper")
    public DeferredResult<RetResult<String>> wiper(String channelId, String command){

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        FrontEndControlCodeForWiper controlCode = new FrontEndControlCodeForWiper();

        switch (command){
            case "on":
                controlCode.setCode(1);
                break;
            case "off":
                controlCode.setCode(2);
                break;
            default:
                break;
        }
        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };

        channelControlService.wiper(channel, controlCode, callback);

        return result;
    }

    @Operation(summary = "辅助开关控制指令")

    @Parameter(name = "channelId", description = "通道国标编号", required = true)
    @Parameter(name = "command", description = "控制指令,允许值: on, off", required = true)
    @Parameter(name = "auxiliaryId", description = "开关编号", required = true)
    @GetMapping("/auxiliary")
    public DeferredResult<com.coalbot.module.core.response.RetResult<String>> auxiliarySwitch(String channelId, String command, Integer auxiliaryId){

        CommonGBChannel channel = channelService.getOne(channelId);
        Assert.notNull(channel, "通道不存在");

        FrontEndControlCodeForAuxiliary controlCode = new FrontEndControlCodeForAuxiliary();
        controlCode.setAuxiliaryId(auxiliaryId);
        switch (command){
            case "on":
                controlCode.setCode(1);
                break;
            case "off":
                controlCode.setCode(2);
                break;
            default:
                break;
        }
        DeferredResult<RetResult<String>> result = new DeferredResult<>();

        result.onTimeout(()->{
            RetResult<String> RetResult = RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "请求超时", "");
            result.setResult(RetResult);
        });

        ErrorCallback<String> callback = (code, msg, data) -> {
            RetResult<String> RetResult = new RetResult<>();
            RetResult.setCode(code);
            RetResult.setMsg(msg);
            RetResult.setData(data);
            result.setResult(RetResult);
        };
        channelControlService.auxiliary(channel, controlCode, callback);
        return result;
    }
}
