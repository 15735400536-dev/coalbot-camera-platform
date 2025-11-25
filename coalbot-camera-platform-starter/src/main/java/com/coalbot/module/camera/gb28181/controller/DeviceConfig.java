/**
 * 设备设置命令API接口
 * 
 * @author lawrencehj
 * @date 2021年2月2日
 */

package com.coalbot.module.camera.gb28181.controller;


import com.coalbot.module.camera.gb28181.bean.BasicParam;
import com.coalbot.module.camera.gb28181.bean.Device;
import com.coalbot.module.camera.gb28181.service.IDeviceService;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Tag(name = "国标设备配置")
@RestController
@RequestMapping("/api/device/config")
public class DeviceConfig {

    @Autowired
    private IDeviceService deviceService;

	@GetMapping("/basicParam")
	@Operation(summary = "基本配置设置命令")
	@Parameter(name = "basicParam", description = "基础配置参数", required = true)
	public DeferredResult<RetResult<String>> homePositionApi(BasicParam basicParam) {
        if (log.isDebugEnabled()) {
			log.debug("基本配置设置命令API调用");
		}
		AssertUtils.notNull(basicParam.getDeviceId(), "设备ID必须存在");

		Device device = deviceService.getDeviceByDeviceId(basicParam.getDeviceId());
		AssertUtils.notNull(device, "设备不存在");

		DeferredResult<RetResult<String>> deferredResult = new DeferredResult<>();
		deviceService.deviceBasicConfig(device, basicParam, (code, msg, data) -> {
			deferredResult.setResult(RetResponse.makeRsp(code, msg, data));
		});

		deferredResult.onTimeout(() -> {
			log.warn("[设备配置] 超时, {}", device.getDeviceId());
			deferredResult.setResult(RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "超时"));
		});
		return deferredResult;

	}

	@Operation(summary = "设备配置查询")
	@Parameter(name = "deviceId", description = "设备国标编号", required = true)
	@Parameter(name = "channelId", description = "通道国标编号", required = true)
	@Parameter(name = "configType", description = "配置类型， 可选值，" +
			"基本参数配置:BasicParam," +
			"视频参数范围:VideoParamOpt, " +
			"SVAC编码配置:SVACEncodeConfig, " +
			"SVAC解码配置:SVACDecodeConfig。" +
			"可同时查询多个配置类型,各类型以“/”分隔，")
	@GetMapping("/query")
    public DeferredResult<RetResult<Object>> configDownloadApi(String deviceId,String configType,
													   @RequestParam(required = false) String channelId) {
		if (log.isDebugEnabled()) {
			log.debug("设备配置查询请求API调用");
		}
		Device device = deviceService.getDeviceByDeviceId(deviceId);
		AssertUtils.notNull(device, "设备不存在");

		DeferredResult<RetResult<Object>> deferredResult = new DeferredResult<>();

		deviceService.deviceConfigQuery(device, channelId, configType, (code, msg, data) -> {
			deferredResult.setResult(RetResponse.makeRsp(code, msg, data));
		});

		deferredResult.onTimeout(() -> {
			log.warn("[获取设备配置] 超时, {}", device.getDeviceId());
			deferredResult.setResult(RetResponse.makeRsp(ErrorCode.ERROR100.getCode(), "超时"));
		});
		return deferredResult;
	}

}
