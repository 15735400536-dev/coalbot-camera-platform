package com.coalbot.module.camera.gb28181.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DrawThinProcess {

    private double process;
    private String msg;

    public DrawThinProcess(double process, String msg) {
        this.process = process;
        this.msg = msg;
    }
}
