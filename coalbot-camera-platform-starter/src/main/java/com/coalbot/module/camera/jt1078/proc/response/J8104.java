package com.coalbot.module.camera.jt1078.proc.response;

import com.coalbot.module.camera.jt1078.annotation.MsgId;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

/**
 * 查询终端参数
 */
@MsgId(id = "8104")
public class J8104 extends Rs {

    @Override
    public ByteBuf encode() {
        return Unpooled.buffer();
    }

}
