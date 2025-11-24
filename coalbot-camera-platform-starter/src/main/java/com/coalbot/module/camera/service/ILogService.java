package com.coalbot.module.camera.service;

import com.coalbot.module.camera.service.bean.LogFileInfo;

import java.io.File;
import java.util.List;

public interface ILogService {
    List<LogFileInfo> queryList(String query, String startTime, String endTime);

    File getFileByName(String fileName);
}
