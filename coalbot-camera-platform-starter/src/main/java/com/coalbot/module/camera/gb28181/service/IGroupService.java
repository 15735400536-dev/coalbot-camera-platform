package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.gb28181.bean.Group;
import com.coalbot.module.camera.gb28181.bean.GroupTree;
import com.github.pagehelper.PageInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;


public interface IGroupService {

    void add(Group group);

    List<Group> queryAllChildren(String id);

    void update(Group group);

    Group queryGroupByDeviceId(String regionDeviceId);

    List<GroupTree> queryForTree(String query, String parent, Boolean hasChannel);

    boolean delete(String id);

    boolean batchAdd(List<Group> groupList);

    List<Group> getPath(String deviceId, String businessGroup);

    PageInfo<Group> queryList(Integer page, Integer count, String query);

    Group queryGroupByAlias(String groupAlias);

    Map<String, Group> queryGroupByAliasMap();

    void saveByAlias(Collection<Group> groups);
}
