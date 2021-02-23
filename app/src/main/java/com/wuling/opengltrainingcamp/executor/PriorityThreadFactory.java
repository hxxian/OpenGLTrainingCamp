package com.wuling.opengltrainingcamp.executor;

import android.os.Process;

import java.util.concurrent.ThreadFactory;

/**
 * @Author: huang xiao xian
 * @Date: 2020/7/17
 * @Des:
 */
public class PriorityThreadFactory implements ThreadFactory {

    private final int mThreadPriority;

    public PriorityThreadFactory(int threadPriority) {
        this.mThreadPriority = threadPriority;
    }

    @Override
    public Thread newThread(final Runnable task) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Process.setThreadPriority(mThreadPriority);
                } catch (Throwable throwable) {

                }

                task.run();
            }
        };

        return new Thread(runnable);
    }
}