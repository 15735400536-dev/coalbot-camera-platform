package com.coalbot.module.camera.conf.ftpServer;

import java.io.OutputStream;

public interface FileCallback {

    OutputStream run(String path);
}
