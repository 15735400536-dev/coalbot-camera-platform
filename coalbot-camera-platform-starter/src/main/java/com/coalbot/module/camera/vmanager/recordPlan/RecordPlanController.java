package com.coalbot.module.camera.vmanager.recordPlan;

import com.coalbot.module.camera.dto.ChannelQueryDTO;
import com.coalbot.module.camera.dto.CommonGBChannelQueryDTO;
import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.gb28181.service.IDeviceChannelService;
import com.coalbot.module.camera.service.IRecordPlanService;
import com.coalbot.module.camera.service.bean.RecordPlan;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.camera.utils.TypeUtils;
import com.coalbot.module.camera.vmanager.bean.ErrorCode;
import com.coalbot.module.camera.vmanager.recordPlan.bean.RecordPlanParam;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.PageList;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
            throw new CommonException("添加录制计划时，录制计划不可为空");
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
            throw new CommonException("通道ID和国标设备ID不可都为NULL");
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
            throw new CommonException("计划ID不可为NULL");
        }
        return RetResponse.makeOKRsp(recordPlanService.get(planId));
    }

    @ResponseBody
    @PostMapping("/query")
    @Operation(summary = "查询录制计划列表")
    public RetResult<PageInfo<RecordPlan>> query(@RequestBody ChannelQueryDTO param) {
        return RetResponse.makeOKRsp(recordPlanService.query(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getSize()), param.getQuery()));
    }

    @Operation(summary = "分页查询录制计划关联的所有通道")
    @PostMapping("/channel/list")
    @ResponseBody
    public RetResult<PageList<CommonGBChannel>> queryChannelList(@RequestBody CommonGBChannelQueryDTO param) {
        AssertUtils.notNull(param.getPlanId(), "录制计划ID不可为NULL");
        PageInfo<CommonGBChannel> pageResult = recordPlanService.queryChannelList(TypeUtils.longToInt(param.getCurrent()), TypeUtils.longToInt(param.getCurrent()),
                param.getQuery(), param.getChannelType(), param.getOnline(), param.getPlanId(), param.getHasLink());
        return RetResponse.makeOKRsp(TypeUtils.pageInfoToPageList(pageResult));
    }

    @ResponseBody
    @PostMapping("/update")
    @Operation(summary = "更新录制计划")
    @Parameter(name = "plan", description = "计划", required = true)
    public RetResult<Void> update(@RequestBody RecordPlan plan) {
        if (plan == null || plan.getId() == null) {
            throw new CommonException(ErrorCode.ERROR400.getMsg());
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
            throw new CommonException("计划IDID不可为NULL");
        }
        recordPlanService.delete(planId);
        return RetResponse.makeOKRsp();
    }

}
