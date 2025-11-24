package com.coalbot.module.camera.service;

import com.coalbot.module.camera.vmanager.bean.MapConfig;
import com.coalbot.module.camera.vmanager.bean.MapModelIcon;

import java.util.List;

public interface IMapService {

    List<MapConfig> getConfig();

    List<MapModelIcon> getModelList();
}
