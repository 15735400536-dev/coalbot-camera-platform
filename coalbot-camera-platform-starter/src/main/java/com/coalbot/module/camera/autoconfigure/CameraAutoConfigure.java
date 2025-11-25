package com.coalbot.module.camera.autoconfigure;

import com.coalbot.module.camera.autoconfigure.properties.CameraProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: pudding
 * @Date: 2024/9/26 11:49
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(CameraProperties.class)
@ConditionalOnProperty(prefix = "spring.coalbot", name = "camera", havingValue = "true", matchIfMissing = true)
@ComponentScan("com.coalbot.module.camera")
@MapperScan("com.coalbot.module.camera.mapper")
public class CameraAutoConfigure {
}
