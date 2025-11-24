package com.coalbot.module.camera.service.bean;

import lombok.Data;

@Data
public class DownloadFileInfo {

    private String httpPath;
    private String httpsPath;
    private String httpDomainPath;
    private String httpsDomainPath;

}
