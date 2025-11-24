package com.coalbot.module.camera.service;

import com.coalbot.module.camera.gb28181.bean.CommonGBChannel;
import com.coalbot.module.camera.service.bean.RecordPlan;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface IRecordPlanService {


    RecordPlan get(String planId);

    void update(RecordPlan plan);

    void delete(String planId);

    PageInfo<RecordPlan> query(Integer page, Integer count, String query);

    void add(RecordPlan plan);

    void link(List<String> channelIds, String planId);

    PageInfo<CommonGBChannel> queryChannelList(int page, int count, String query, Integer channelType, Boolean online, String planId, Boolean hasLink);

    void linkAll(String planId);

    void cleanAll(String planId);

    String recording(String app, String stream);
}
