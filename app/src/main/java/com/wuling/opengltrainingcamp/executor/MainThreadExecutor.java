package com.wuling.opengltrainingcamp.executor;

import android.os.Handler;
import android.os.Looper;

/**
 * @Author: huang xiao xian
 * @Date: 2020/7/17
 * @Des: UI main 线程任务执行器
 */
public class MainThreadExecutor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public void execute(Runnable command) {
        handler.post(command);
    }

    public void execute(Runnable command, long delay) {
        handler.postDelayed(command, delay);
    }
}