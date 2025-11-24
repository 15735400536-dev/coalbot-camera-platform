package com.coalbot.module.camera.jt1078.proc.response;

import com.coalbot.module.camera.jt1078.annotation.MsgId;
import com.coalbot.module.camera.jt1078.bean.JTQueryMediaDataCommand;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

/**
 * 存储多媒体数据上传命令
 */
@Setter
@Getter
@MsgId(id = "8803")
public class J8803 extends Rs {

    JTQueryMediaDataCommand command;

    @Override
    public ByteBuf encode() {
        return command.decode();
    }

}
