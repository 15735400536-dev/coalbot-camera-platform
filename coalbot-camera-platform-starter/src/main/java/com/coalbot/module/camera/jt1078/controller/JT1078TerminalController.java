package com.coalbot.module.camera.jt1078.controller;


import com.coalbot.module.camera.jt1078.bean.JTChannel;
import com.coalbot.module.camera.jt1078.bean.JTDevice;
import com.coalbot.module.camera.jt1078.service.Ijt1078Service;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@Slf4j
@ConditionalOnProperty(value = "jt1078.enable", havingValue = "true")
@RestController
@Tag(name = "部标终端以及通道管理")
@RequestMapping("/api/jt1078/terminal")
public class JT1078TerminalController {

    @Resource
    Ijt1078Service service;

    @Operation(summary = "JT-分页查询部标设备")
    @Parameter(name = "page", description = "当前页", required = true)
    @Parameter(name = "count", description = "每页查询数量", required = true)
    @Parameter(name = "query", description = "查询内容")
    @Parameter(name = "online", description = "是否在线")
    @GetMapping("/list")
    public PageInfo<JTDevice> getDevices(int page, int count,
                                         @RequestParam(required = false) String query,
                                         @RequestParam(required = false) Boolean online) {
        return service.getDeviceList(page, count, query, online);
    }

    @Operation(summary = "更新设备")
    @Parameter(name = "device", description = "设备", required = true)
    @PostMapping("/update")
    public RetResult<Void> updateDevice(JTDevice device) {
        assert device.getId() != null;
        assert device.getPhoneNumber() != null;
        service.updateDevice(device);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "JT-新增设备")
    @Parameter(name = "device", description = "设备", required = true)
    @PostMapping("/add")
    public RetResult<Void> addDevice(JTDevice device) {
        assert device.getPhoneNumber() != null;
        String phoneNumber = device.getPhoneNumber().replaceFirst("^0*", "");
        device.setPhoneNumber(phoneNumber);
        service.addDevice(device);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "删除设备")
    @Parameter(name = "phoneNumber", description = "设备手机号", required = true)
    @DeleteMapping("/delete")
    public RetResult<Void> addDevice(String phoneNumber) {
        assert phoneNumber != null;
        service.deleteDeviceByPhoneNumber(phoneNumber);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "查询设备")
    @Parameter(name = "phoneNumber", description = "设备手机号", required = true)
    @GetMapping("/query")
    public JTDevice getDevice(String deviceId) {
        return service.getDeviceById(deviceId);
    }


    @Operation(summary = "JT-查询部标通道")
    @Parameter(name = "page", description = "当前页", required = true)
    @Parameter(name = "count", description = "每页查询数量", required = true)
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @Parameter(name = "query", description = "查询内容")
    @GetMapping("/channel/list")
    public PageInfo<JTChannel> getChannels(int page, int count,
                                           @RequestParam(required = true) String deviceId,
                                           @RequestParam(required = false) String query) {
        assert deviceId != null;
        return service.getChannelList(page, count, deviceId, query);
    }

    @Operation(summary = "JT-查询单个部标通道")
    @Parameter(name = "id", description = "通道数据库ID", required = true)
    @GetMapping("/channel/one")
    public JTChannel getChannel(String id) {
        assert id != null;
        return service.getChannelByDbId(id);
    }

    @Operation(summary = "JT-更新通道")
    @Parameter(name = "channel", description = "通道", required = true)
    @PostMapping("/channel/update")
    public RetResult<Void> updateChannel(@RequestBody JTChannel channel) {
        assert channel.getId() != null;
        assert channel.getChannelId() != null;
        service.updateChannel(channel);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "JT-新增通道")
    @Parameter(name = "channel", description = "通道", required = true)
    @PostMapping("/channel/add")
    public JTChannel addChannel(@RequestBody JTChannel channel) {
        assert channel.getChannelId() != null;
        assert channel.getTerminalDbId() != null;
        service.addChannel(channel);
        return channel;
    }

    @Operation(summary = "JT-删除通道")
    @Parameter(name = "id", description = "通道的数据库ID", required = true)
    @DeleteMapping("/channel/delete")
    public RetResult<Void> deleteChannel(String id) {
        service.deleteChannelById(id);
        return RetResponse.makeOKRsp();
    }
}

