package com.lun.autoscript.interceptor;

import static com.lun.auto.accessibility.global_fun_api.log;

import androidx.annotation.NonNull;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable e) {
        System.out.println("11111");
        // 处理异常
        log(e);
    }
}
