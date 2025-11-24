package com.coalbot.module.camera.jt1078.proc.response;

import com.coalbot.module.camera.jt1078.annotation.MsgId;
import com.coalbot.module.camera.jt1078.bean.JTQueryMediaDataCommand;
import com.coalbot.module.camera.jt1078.bean.JTShootingCommand;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * 存储多媒体数据检索
 */
@Setter
@Getter
@MsgId(id = "8802")
public class J8802 extends Rs {

    JTQueryMediaDataCommand command;

    @Override
    public ByteBuf encode() {
        return command.decode();
    }

}
