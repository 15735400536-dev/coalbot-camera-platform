package com.coalbot.module.camera.vmanager.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.coalbot.module.camera.common.SystemAllInfo;
import com.coalbot.module.camera.common.VersionPo;
import com.coalbot.module.camera.conf.SipConfig;
import com.coalbot.module.camera.conf.UserSetting;
import com.coalbot.module.camera.conf.VersionInfo;
import com.coalbot.module.camera.gb28181.service.IDeviceChannelService;
import com.coalbot.module.camera.gb28181.service.IDeviceService;
import com.coalbot.module.camera.jt1078.config.JT1078Config;
import com.coalbot.module.camera.media.bean.MediaInfo;
import com.coalbot.module.camera.media.bean.MediaServer;
import com.coalbot.module.camera.media.event.mediaServer.MediaServerChangeEvent;
import com.coalbot.module.camera.media.service.IMediaServerService;
import com.coalbot.module.camera.service.IMapService;
import com.coalbot.module.camera.service.bean.MediaServerLoad;
import com.coalbot.module.camera.storager.IRedisCatchStorage;
import com.coalbot.module.camera.streamProxy.service.IStreamProxyService;
import com.coalbot.module.camera.streamPush.service.IStreamPushService;
import com.coalbot.module.camera.vmanager.bean.*;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;

@SuppressWarnings("rawtypes")
@Tag(name = "服务控制")
@Slf4j
@RestController
@RequestMapping("/api/server")
public class ServerController {


    @Autowired
    private IMediaServerService mediaServerService;

    @Autowired
    private VersionInfo versionInfo;

    @Autowired
    private SipConfig sipConfig;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private JT1078Config jt1078Config;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceChannelService channelService;

    @Autowired
    private IStreamPushService pushService;

    @Autowired
    private IStreamProxyService proxyService;


    @Autowired(required = false)
    private IMapService mapService;

    @Value("${server.port}")
    private int serverPort;


    @Autowired
    private IRedisCatchStorage redisCatchStorage;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @GetMapping(value = "/media_server/list")
    @ResponseBody
    @Operation(summary = "流媒体服务列表")
    public RetResult<List<MediaServer>> getMediaServerList() {
        return RetResponse.makeOKRsp(mediaServerService.getAll());
    }

    @GetMapping(value = "/media_server/online/list")
    @ResponseBody
    @Operation(summary = "在线流媒体服务列表")
    public RetResult<List<MediaServer>> getOnlineMediaServerList() {
        return RetResponse.makeOKRsp(mediaServerService.getAllOnline());
    }

    @GetMapping(value = "/media_server/one/{id}")
    @ResponseBody
    @Operation(summary = "停止视频回放")
    @Parameter(name = "id", description = "流媒体服务ID", required = true)
    public RetResult<MediaServer> getMediaServer(@PathVariable String id) {
        return RetResponse.makeOKRsp(mediaServerService.getOne(id));
    }

    @Operation(summary = "测试流媒体服务")
    @Parameter(name = "ip", description = "流媒体服务IP", required = true)
    @Parameter(name = "port", description = "流媒体服务HTT端口", required = true)
    @Parameter(name = "secret", description = "流媒体服务secret", required = true)
    @GetMapping(value = "/media_server/check")
    @ResponseBody
    public RetResult<MediaServer> checkMediaServer(@RequestParam String ip, @RequestParam int port, @RequestParam String secret, @RequestParam String type) {
        return RetResponse.makeOKRsp(mediaServerService.checkMediaServer(ip, port, secret, type));
    }

    @Operation(summary = "测试流媒体录像管理服务")
    @Parameter(name = "ip", description = "流媒体服务IP", required = true)
    @Parameter(name = "port", description = "流媒体服务HTT端口", required = true)
    @GetMapping(value = "/media_server/record/check")
    @ResponseBody
    public RetResult<Void> checkMediaRecordServer(@RequestParam String ip, @RequestParam int port) {
        boolean checkResult = mediaServerService.checkMediaRecordServer(ip, port);
        if (!checkResult) {
            throw new CommonException("连接失败");
        }
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "保存流媒体服务")
    @Parameter(name = "mediaServerItem", description = "流媒体信息", required = true)
    @PostMapping(value = "/media_server/save")
    @ResponseBody
    public RetResult<Void> saveMediaServer(@RequestBody MediaServer mediaServer) {
        MediaServer mediaServerItemInDatabase = mediaServerService.getOneFromDatabase(mediaServer.getId());

        if (mediaServerItemInDatabase != null) {
            mediaServerService.update(mediaServer);
        } else {
            mediaServerService.add(mediaServer);
            // 发送事件
            MediaServerChangeEvent event = new MediaServerChangeEvent(this);
            event.setMediaServerItemList(mediaServer);
            applicationEventPublisher.publishEvent(event);
        }
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "移除流媒体服务")
    @Parameter(name = "id", description = "流媒体ID", required = true)
    @DeleteMapping(value = "/media_server/delete")
    @ResponseBody
    public RetResult<Void> deleteMediaServer(@RequestParam String id) {
        MediaServer mediaServer = mediaServerService.getOne(id);
        if (mediaServer == null) {
            throw new CommonException("流媒体不存在");
        }
        mediaServerService.delete(mediaServer);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "获取流信息")
    @Parameter(name = "app", description = "应用名", required = true)
    @Parameter(name = "stream", description = "流ID", required = true)
    @Parameter(name = "mediaServerId", description = "流媒体ID", required = true)
    @GetMapping(value = "/media_server/media_info")
    @ResponseBody
    public RetResult<MediaInfo> getMediaInfo(String app, String stream, String mediaServerId) {
        MediaServer mediaServer = mediaServerService.getOneFromCluster(mediaServerId);
        if (mediaServer == null) {
            throw new CommonException("流媒体不存在");
        }
        return RetResponse.makeOKRsp(mediaServerService.getMediaInfo(mediaServer, app, stream));
    }


    @Operation(summary = "关闭服务")
    @GetMapping(value = "/shutdown")
    @ResponseBody
    public RetResult<Void> shutdown() {
        log.info("正在关闭服务。。。");
        System.exit(1);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "获取系统配置信息")
    @GetMapping(value = "/system/configInfo")
    @ResponseBody
    public RetResult<SystemConfigInfo> getConfigInfo() {
        SystemConfigInfo systemConfigInfo = new SystemConfigInfo();
        systemConfigInfo.setVersion(versionInfo.getVersion());
        systemConfigInfo.setSip(sipConfig);
        systemConfigInfo.setAddOn(userSetting);
        systemConfigInfo.setServerPort(serverPort);
        systemConfigInfo.setJt1078Config(jt1078Config);
        return RetResponse.makeOKRsp(systemConfigInfo);
    }

    @Operation(summary = "获取版本信息")
    @GetMapping(value = "/version")
    @ResponseBody
    public RetResult<VersionPo> VersionPogetVersion() {
        return RetResponse.makeOKRsp(versionInfo.getVersion());
    }

    @GetMapping(value = "/config")
    @Operation(summary = "获取配置信息")
    @Parameter(name = "type", description = "配置类型（sip, base）", required = true)
    @ResponseBody
    public RetResult<JSONObject> getVersion(String type) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("server.port", serverPort);
        if (ObjectUtils.isEmpty(type)) {
            jsonObject.put("sip", JSON.toJSON(sipConfig));
            jsonObject.put("base", JSON.toJSON(userSetting));
        } else {
            switch (type) {
                case "sip":
                    jsonObject.put("sip", sipConfig);
                    break;
                case "base":
                    jsonObject.put("base", userSetting);
                    break;
                default:
                    break;
            }
        }
        return RetResponse.makeOKRsp(jsonObject);
    }

    @GetMapping(value = "/system/info")
    @ResponseBody
    @Operation(summary = "获取系统信息")
    public RetResult<SystemAllInfo> getSystemInfo() {
        SystemAllInfo systemAllInfo = redisCatchStorage.getSystemInfo();
        return RetResponse.makeOKRsp(systemAllInfo);
    }

    @GetMapping(value = "/media_server/load")
    @ResponseBody
    @Operation(summary = "获取负载信息")
    public RetResult<List<MediaServerLoad>> getMediaLoad() {
        List<MediaServerLoad> result = new ArrayList<>();
        List<MediaServer> allOnline = mediaServerService.getAllOnline();
        if (allOnline.isEmpty()) {
            return RetResponse.makeOKRsp(result);
        } else {
            for (MediaServer mediaServerItem : allOnline) {
                result.add(mediaServerService.getLoad(mediaServerItem));
            }
        }
        return RetResponse.makeOKRsp(result);
    }

    @GetMapping(value = "/resource/info")
    @ResponseBody
    @Operation(summary = "获取负载信息")
    public RetResult<ResourceInfo> getResourceInfo() {
        ResourceInfo result = new ResourceInfo();
        ResourceBaseInfo deviceInfo = deviceService.getOverview();
        result.setDevice(deviceInfo);
        ResourceBaseInfo channelInfo = channelService.getOverview();
        result.setChannel(channelInfo);
        ResourceBaseInfo pushInfo = pushService.getOverview();
        result.setPush(pushInfo);
        ResourceBaseInfo proxyInfo = proxyService.getOverview();
        result.setProxy(proxyInfo);
        return RetResponse.makeOKRsp(result);
    }

    @GetMapping(value = "/info")
    @ResponseBody
    @Operation(summary = "获取系统信息")
    public RetResult<Map<String, Map<String, String>>> getInfo(HttpServletRequest request) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> hardwareMap = new LinkedHashMap<>();
        result.put("硬件信息", hardwareMap);

        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        // 获取CPU信息
        CentralProcessor.ProcessorIdentifier processorIdentifier = hardware.getProcessor().getProcessorIdentifier();
        hardwareMap.put("CPU", processorIdentifier.getName());
        // 获取内存
        GlobalMemory memory = hardware.getMemory();
        hardwareMap.put("内存", formatByte(memory.getTotal() - memory.getAvailable()) + "/" + formatByte(memory.getTotal()));
        hardwareMap.put("制造商", systemInfo.getHardware().getComputerSystem().getManufacturer());
        hardwareMap.put("产品名称", systemInfo.getHardware().getComputerSystem().getModel());
        // 网卡
        List<NetworkIF> networkIFs = hardware.getNetworkIFs();
        StringBuilder ips = new StringBuilder();
        for (int i = 0; i < networkIFs.size(); i++) {
            NetworkIF networkIF = networkIFs.get(i);
            String ipsStr = StringUtils.join(networkIF.getIPv4addr());
            if (ObjectUtils.isEmpty(ipsStr)) {
                continue;
            }
            ips.append(ipsStr);
            if (i < networkIFs.size() - 1) {
                ips.append(",");
            }
        }
        hardwareMap.put("网卡", ips.toString());

        Map<String, String> operatingSystemMap = new LinkedHashMap<>();
        result.put("操作系统", operatingSystemMap);
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        operatingSystemMap.put("名称", operatingSystem.getFamily() + " " + operatingSystem.getVersionInfo().getVersion());
        operatingSystemMap.put("类型", operatingSystem.getManufacturer());

        Map<String, String> platformMap = new LinkedHashMap<>();
        result.put("平台信息", platformMap);
        VersionPo version = versionInfo.getVersion();
        platformMap.put("版本", version.getVersion());
        platformMap.put("构建日期", version.getBUILD_DATE());
        platformMap.put("GIT分支", version.getGIT_BRANCH());
        platformMap.put("GIT地址", version.getGIT_URL());
        platformMap.put("GIT日期", version.getGIT_DATE());
        platformMap.put("GIT版本", version.getGIT_Revision_SHORT());
        platformMap.put("DOCKER环境", new File("/.dockerenv").exists() ? "是" : "否");

        Map<String, String> docmap = new LinkedHashMap<>();
        result.put("文档地址", docmap);
        docmap.put("部署文档", "https://doc.wvp-pro.cn");
        docmap.put("接口文档", String.format("%s://%s:%s/doc.html", request.getScheme(), request.getServerName(), request.getServerPort()));
        return RetResponse.makeOKRsp(result);
    }

    /**
     * 单位转换
     */
    private static String formatByte(long byteNumber) {
        //换算单位
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.##KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.##MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.##GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.##TB").format(tbNumber);
    }

    @GetMapping(value = "/map/config")
    @ResponseBody
    @Operation(summary = "获取地图配置")
    public RetResult<List<MapConfig>> getMapConfig() {
        if (mapService == null) {
            return RetResponse.makeOKRsp(Collections.emptyList());
        }
        return RetResponse.makeOKRsp(mapService.getConfig());
    }

    @GetMapping(value = "/map/model-icon/list")
    @ResponseBody
    @Operation(summary = "获取地图配置图标")
    public RetResult<List<MapModelIcon>> getMapModelIconList() {
        if (mapService == null) {
            return RetResponse.makeOKRsp(Collections.emptyList());
        }
        return RetResponse.makeOKRsp(mapService.getModelList());
    }
}
