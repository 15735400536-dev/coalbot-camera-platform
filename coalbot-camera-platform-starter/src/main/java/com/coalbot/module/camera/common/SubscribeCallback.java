package com.coalbot.module.camera.common;

import com.coalbot.module.camera.gb28181.bean.SipTransactionInfo;

public interface SubscribeCallback{
    public void run(String deviceId, SipTransactionInfo transactionInfo);
}
