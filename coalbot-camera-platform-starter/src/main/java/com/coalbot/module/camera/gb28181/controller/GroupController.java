package com.coalbot.module.camera.gb28181.controller;

import com.coalbot.module.camera.gb28181.bean.Group;
import com.coalbot.module.camera.gb28181.bean.GroupTree;
import com.coalbot.module.camera.gb28181.service.IGroupService;
import com.coalbot.module.camera.utils.AssertUtils;
import com.coalbot.module.core.exception.CommonException;
import com.coalbot.module.core.response.RetResponse;
import com.coalbot.module.core.response.RetResult;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "分组管理")
@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private IGroupService groupService;

    @Operation(summary = "添加分组")
    @Parameter(name = "group", description = "group", required = true)
    @ResponseBody
    @PostMapping("/add")
    public RetResult<Void> add(@RequestBody Group group){
        groupService.add(group);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "查询分组节点")
    @Parameter(name = "query", description = "要搜索的内容", required = true)
    @Parameter(name = "parent", description = "所属分组编号", required = true)
    @ResponseBody
    @GetMapping("/tree/list")
    public RetResult<List<GroupTree>> queryForTree(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String parent,
            @RequestParam(required = false) Boolean hasChannel
    ){
        if (ObjectUtils.isEmpty(query)) {
            query = null;
        }
        return RetResponse.makeOKRsp(groupService.queryForTree(query, parent, hasChannel));
    }

    @Operation(summary = "查询分组")
    @Parameter(name = "query", description = "要搜索的内容", required = true)
    @Parameter(name = "channel", description = "true为查询通道，false为查询节点", required = true)
    @ResponseBody
    @GetMapping("/tree/query")
    public RetResult<PageInfo<Group>> queryTree(Integer page, Integer count,
                                      @RequestParam(required = true) String query
    ){
        return RetResponse.makeOKRsp(groupService.queryList(page, count, query));
    }

    @Operation(summary = "更新分组")
    @Parameter(name = "group", description = "Group", required = true)
    @ResponseBody
    @PostMapping("/update")
    public RetResult<Void> update(@RequestBody Group group){
        groupService.update(group);
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "删除分组")
    @Parameter(name = "id", description = "分组id", required = true)
    @ResponseBody
    @DeleteMapping("/delete")
    public RetResult<Void> delete(String id){
        AssertUtils.notNull(id, "分组id（deviceId）不需要存在");
        boolean result = groupService.delete(id);
        if (!result) {
            throw new CommonException("移除失败");
        }
        return RetResponse.makeOKRsp();
    }

    @Operation(summary = "获取所属的行政区划下的行政区划")
    @Parameter(name = "deviceId", description = "当前的行政区划", required = false)
    @ResponseBody
    @GetMapping("/path")
    public RetResult<List<Group>> getPath(String deviceId, String businessGroup){
        return RetResponse.makeOKRsp(groupService.getPath(deviceId, businessGroup));
    }

//    @Operation(summary = "根据分组Id查询分组")
//    @Parameter(name = "groupDeviceId", description = "分组节点编号", required = true)
//    @ResponseBody
//    @GetMapping("/one")
//    public Group queryGroupByDeviceId(
//            @RequestParam(required = true) String deviceId
//    ){
//        AssertUtils.notBlank(deviceId, "");
//        return groupService.queryGroupByDeviceId(deviceId);
//    }

//    @Operation(summary = "从通道中同步分组")
//    @ResponseBody
//    @GetMapping("/sync")
//    public RetResult<Void> sync(){
//        groupService.syncFromChannel();
//    }
}
