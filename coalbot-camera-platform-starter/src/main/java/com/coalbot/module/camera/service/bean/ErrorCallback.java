package com.coalbot.module.camera.service.bean;

public interface ErrorCallback<T> {

    void run(int code, String msg, T data);
}
