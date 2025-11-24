package com.coalbot.module.camera.vmanager.recordPlan;

import com.coalbot.module.camera.conf.exception.ControllerException;
import com.coalbot.module.camera.dto.ChannelQueryDTO;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.service.IDeviceChannelService;
import com.coalbot.module.camera.service.IRecordPlanService;
import com.coalbot.module.camera.service.bean.RecordPlan;
import com.coalbot.module.camera.utils.TypeUtils;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.camera.vmanager.recordPlan.bean.RecordPlanParam;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "录制计划")
@Slf4j
@RestController
@RequestMapping("/api/record/plan")
public class RecordPlanController {

    @Autowired
    private IRecordPlanService recordPlanService;

    @Autowired
    private IDeviceChannelService deviceChannelService;


    @ResponseBody
    @PostMapping("/add")
    @Operation(summary = "添加录制计划")
    @Parameter(name = "plan", description = "计划", required = true)
    public RetResult<Void> add(@RequestBody RecordPlan plan) {
        if (plan.getPlanItemList() == null || plan.getPlanItemList().isEmpty()) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "添加录制计划时，录制计划不可为空");
        }
        recordPlanService.add(plan);
        return RetResponse.makeOKRsp();
    }

    @ResponseBody
    @PostMapping("/link")
    @Operation(summary = "通道关联录制计划")
    @Parameter(name = "param", description = "通道关联录制计划", required = true)
    public RetResult<Void> link(@RequestBody RecordPlanParam param) {
        if (param.getAllLink() != null) {
            if (param.getAllLink()) {
                recordPlanService.linkAll(param.getPlanId());
            } else {
                recordPlanService.cleanAll(param.getPlanId());
            }
            return RetResponse.makeOKRsp();
        }

        if (param.getChannelIds() == null && param.getDeviceDbIds() == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "通道ID和国标设备ID不可都为NULL");
        }

        List<String> channelIds = new ArrayList<>();
        if (param.getChannelIds() != null) {
            channelIds.addAll(param.getChannelIds());
        } else {
            List<String> chanelIdList = deviceChannelService.queryChaneIdListByDeviceDbIds(param.getDeviceDbIds());
            if (chanelIdList != null && !chanelIdList.isEmpty()) {
                channelIds = chanelIdList;
            }
        }
        recordPlanService.link(channelIds, param.getPlanId());
        return RetResponse.makeOKRsp();
    }

    @ResponseBody
    @GetMapping("/get")
    @Operation(summary = "查询录制计划")
    @Parameter(name = "planId", description = "计划ID", required = true)
    public RetResult<RecordPlan> get(String planId) {
        if (planId == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "计划ID不可为NULL");
        }
        return RetResponse.makeOKRsp(recordPlanService.get(planId));
    }

//    @ResponseBody
//    @GetMapping("/query")
//    @Operation(summary = "查询录制计划列表")
//    @Parameter(name = "query", description = "检索内容", required = false)
//    @Parameter(name = "page", description = "当前页", required = true)
//    @Parameter(name = "count", description = "每页查询数量", required = true)
//    public RetResult<PageInfo<RecordPlan>> query(@RequestParam(required = false) String query, @RequestParam Integer page, @RequestParam Integer count) {
//        if (query != null && ObjectUtils.isEmpty(query.trim())) {
//            query = null;
//        }
//        return RetResponse.makeOKRsp(recordPlanService.query(page, count, query));
//    }

    @ResponseBody
    @PostMapping("/query")
    @Operation(summary = "查询录制计划列表")
    public RetResult<PageInfo<RecordPlan>> query(@RequestBody ChannelQueryDTO param) {
        return RetResponse.makeOKRsp(recordPlanService.query(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getSize()), param.getQuery()));
    }

//    @Operation(summary = "分页查询录制计划关联的所有通道")
//    @Parameter(name = "page", description = "当前页", required = true)
//    @Parameter(name = "count", description = "每页条数", required = true)
//    @Parameter(name = "planId", description = "录制计划ID")
//    @Parameter(name = "channelType", description = "通道类型， 0：国标设备，1：推流设备，2：拉流代理")
//    @Parameter(name = "query", description = "查询内容")
//    @Parameter(name = "online", description = "是否在线")
//    @Parameter(name = "hasLink", description = "是否已经关联")
//    @GetMapping("/channel/list")
//    @ResponseBody
//    public RetResult<PageInfo<CommonGBChannel>> queryChannelList(int page, int count,
//                                                                 @RequestParam(required = false) String planId,
//                                                                 @RequestParam(required = false) String query,
//                                                                 @RequestParam(required = false) Integer channelType,
//                                                                 @RequestParam(required = false) Boolean online,
//                                                                 @RequestParam(required = false) Boolean hasLink) {
//
//        Assert.notNull(planId, "录制计划ID不可为NULL");
//        if (org.springframework.util.ObjectUtils.isEmpty(query)) {
//            query = null;
//        }
//
//        return RetResponse.makeOKRsp(recordPlanService.queryChannelList(page, count, query, channelType, online, planId, hasLink));
//    }

    @Operation(summary = "分页查询录制计划关联的所有通道")
    @Parameter(name = "page", description = "当前页", required = true)
    @Parameter(name = "count", description = "每页条数", required = true)
    @Parameter(name = "planId", description = "录制计划ID")
    @Parameter(name = "channelType", description = "通道类型， 0：国标设备，1：推流设备，2：拉流代理")
    @Parameter(name = "query", description = "查询内容")
    @Parameter(name = "online", description = "是否在线")
    @Parameter(name = "hasLink", description = "是否已经关联")
    @GetMapping("/channel/list")
    @ResponseBody
    public RetResult<PageInfo<CommonGBChannel>> queryChannelList(int page, int count,
                                                                 @RequestParam(required = false) String planId,
                                                                 @RequestParam(required = false) String query,
                                                                 @RequestParam(required = false) Integer channelType,
                                                                 @RequestParam(required = false) Boolean online,
                                                                 @RequestParam(required = false) Boolean hasLink) {

        Assert.notNull(planId, "录制计划ID不可为NULL");
        if (org.springframework.util.ObjectUtils.isEmpty(query)) {
            query = null;
        }

        return RetResponse.makeOKRsp(recordPlanService.queryChannelList(page, count, query, channelType, online, planId, hasLink));
    }

    @ResponseBody
    @PostMapping("/update")
    @Operation(summary = "更新录制计划")
    @Parameter(name = "plan", description = "计划", required = true)
    public RetResult<Void> update(@RequestBody RecordPlan plan) {
        if (plan == null || plan.getId() == null) {
            throw new ControllerException(ErrorCode.ERROR400);
        }
        recordPlanService.update(plan);
        return RetResponse.makeOKRsp();
    }

    @ResponseBody
    @DeleteMapping("/delete")
    @Operation(summary = "删除录制计划")
    @Parameter(name = "planId", description = "计划ID", required = true)
    public RetResult<Void> delete(String planId) {
        if (planId == null) {
            throw new ControllerException(ErrorCode.ERROR100.getCode(), "计划IDID不可为NULL");
        }
        recordPlanService.delete(planId);
        return RetResponse.makeOKRsp();
    }

}
