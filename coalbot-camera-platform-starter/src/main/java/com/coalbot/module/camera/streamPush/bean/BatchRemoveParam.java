package com.coalbot.module.camera.streamPush.bean;

import lombok.Data;

import java.util.Set;

@Data
public class BatchRemoveParam {
    private Set<String> ids;
}
