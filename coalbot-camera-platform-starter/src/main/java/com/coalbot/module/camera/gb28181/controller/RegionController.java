package com.coalbot.module.camera.gb28181.controller;

import com.coalbot.module.camera.dto.GbRegionQueryDTO;
import com.coalbot.module.camera.gb28181.bean.Region;
import com.coalbot.module.camera.gb28181.bean.RegionTree;
import com.coalbot.module.camera.gb28181.service.IRegionService;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.camera.utils.TypeUtils;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.PageList;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "区域管理")
@RestController
@RequestMapping("/api/region")
public class RegionController {

    @Autowired
    private IRegionService regionService;

    @Operation(summary = "添加区域")
    @Parameter(name = "region", description = "Region", required = true)
    @ResponseBody
    @PostMapping("/add")
    public RetResult<Void> add(@RequestBody Region region) {
        regionService.add(region);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "查询区域")
    @ResponseBody
    @PostMapping("/page/list")
    public RetResult<PageList<Region>> query(@RequestBody GbRegionQueryDTO dto) {
        PageInfo<Region> pageResult = regionService.query(dto.getQuery(),
                TypeUtils.longToInt(dto.getCurrent()), TypeUtils.longToInt(dto.getSize()));
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "查询区域节点")
    @Parameter(name = "query", description = "要搜索的内容", required = true)
    @Parameter(name = "parent", description = "所属行政区划编号", required = true)
    @Parameter(name = "hasChannel", description = "是否查询通道", required = true)
    @ResponseBody
    @GetMapping("/tree/list")
    public RetResult<List<RegionTree>> queryForTree(
            @RequestParam(required = false) String parent,
            @RequestParam(required = false) Boolean hasChannel
    ) {
        return RetResponse.makeOKRsp(regionService.queryForTree(parent, hasChannel));
    }

    @Operation(summary = "查询区域")
    @ResponseBody
    @PostMapping("/tree/query")
    public RetResult<PageList<Region>> queryTree(@RequestBody GbRegionQueryDTO dto) {
        PageInfo<Region> pageResult = regionService.queryList(TypeUtils.longToInt(dto.getCurrent()), TypeUtils.longToInt(dto.getSize()), dto.getQuery());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @Operation(summary = "更新区域")
    @Parameter(name = "region", description = "Region", required = true)
    @ResponseBody
    @PostMapping("/update")
    public RetResult<Void> update(@RequestBody Region region) {
        regionService.update(region);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "删除区域")
    @Parameter(name = "id", description = "区域ID", required = true)
    @ResponseBody
    @DeleteMapping("/delete")
    public RetResult<Void> delete(String id) {
        AssertUtils.notNull(id, "区域ID需要存在");
        boolean result = regionService.deleteByDeviceId(id);
        if (!result) {
            throw new CommonException("移除失败");
        }
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "根据区域Id查询区域")
    @Parameter(name = "regionDeviceId", description = "行政区划节点编号", required = true)
    @ResponseBody
    @GetMapping("/one")
    public RetResult<Region> queryRegionByDeviceId(
            @RequestParam(required = true) String regionDeviceId
    ) {
        if (ObjectUtils.isEmpty(regionDeviceId.trim())) {
            throw new CommonException(ErrorCode.ERROR400.getMsg());
        }
        return RetResponse.makeOKRsp(regionService.queryRegionByDeviceId(regionDeviceId));
    }

    @Operation(summary = "获取所属的行政区划下的行政区划")
    @Parameter(name = "parent", description = "所属的行政区划", required = false)
    @ResponseBody
    @GetMapping("/base/child/list")
    public RetResult<List<Region>> getAllChild(@RequestParam(required = false) String parent) {
        if (ObjectUtils.isEmpty(parent)) {
            parent = null;
        }
        return RetResponse.makeOKRsp(regionService.getAllChild(parent));
    }

    @Operation(summary = "获取所属的行政区划下的行政区划")
    @Parameter(name = "deviceId", description = "当前的行政区划", required = false)
    @ResponseBody
    @GetMapping("/path")
    public RetResult<List<Region>> getPath(String deviceId) {
        return RetResponse.makeOKRsp(regionService.getPath(deviceId));
    }

    @Operation(summary = "从通道中同步行政区划")
    @ResponseBody
    @GetMapping("/sync")
    public RetResult<Void> sync() {
        regionService.syncFromChannel();
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "根据行政区划编号从文件中查询层级和描述")
    @ResponseBody
    @GetMapping("/description")
    public RetResult<String> getDescription(String civilCode) {
        return RetResponse.makeOKRsp(regionService.getDescription(civilCode));
    }

    @Operation(summary = "根据行政区划编号从文件中查询层级并添加")
    @ResponseBody
    @GetMapping("/addByCivilCode")
    public RetResult<Void> addByCivilCode(String civilCode) {
        regionService.addByCivilCode(civilCode);
        return RetResponse.makeOKRsp();
    }

}
