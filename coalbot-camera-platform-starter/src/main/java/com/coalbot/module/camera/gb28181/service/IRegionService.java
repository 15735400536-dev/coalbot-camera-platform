package com.coalbot.module.camera.gb28181.service;

import com.coalbot.module.camera.gb28181.bean.Region;
import com.coalbot.module.camera.gb28181.bean.RegionTree;
import com.github.pagehelper.PageInfo;

import java.util.List;


public interface IRegionService {

    void add(Region region);

    boolean deleteByDeviceId(String regionDeviceId);

    /**
     * 查询区划列表
     */
    PageInfo<Region> query(String query, int page, int count);

    /**
     * 更新区域
     */
    void update(Region region);

    List<Region> getAllChild(String parent);

    Region queryRegionByDeviceId(String regionDeviceId);

    List<RegionTree> queryForTree(String parent, Boolean hasChannel);

    void syncFromChannel();

    boolean delete(String id);

    boolean batchAdd(List<Region> regionList);

    List<Region> getPath(String deviceId);

    String getDescription(String civilCode);

    void addByCivilCode(String civilCode);

    PageInfo<Region> queryList(int page, int count, String query);
}
