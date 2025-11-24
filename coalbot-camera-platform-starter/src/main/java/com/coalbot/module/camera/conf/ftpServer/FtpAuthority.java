package com.coalbot.module.camera.conf.ftpServer;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;

public class FtpAuthority implements Authority {

    @Override
    public boolean canAuthorize(AuthorizationRequest authorizationRequest) {
        return true;
    }

    @Override
    public AuthorizationRequest authorize(AuthorizationRequest authorizationRequest) {
        return authorizationRequest;
    }
}
