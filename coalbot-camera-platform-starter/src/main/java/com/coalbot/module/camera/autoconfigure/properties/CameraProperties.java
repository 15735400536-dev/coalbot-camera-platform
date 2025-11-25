package com.coalbot.module.camera.autoconfigure.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description:
 * @Author: pudding
 * @Date: 2024/12/10 11:06
 * @Version 1.0
 */
@Getter
@Setter
@ConfigurationProperties("spring.coalbot")
public class CameraProperties {

    private boolean camera = true;
}
