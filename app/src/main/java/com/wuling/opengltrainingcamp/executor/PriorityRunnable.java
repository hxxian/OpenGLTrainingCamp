package com.wuling.opengltrainingcamp.executor;

/**
 * @Author: huang xiao xian
 * @Date: 2020/7/17
 * @Des: 具有优先级概念的任务
 */
public class PriorityRunnable implements Runnable {

    private final Priority priority;

    public PriorityRunnable(Priority priority) {
        this.priority = priority;
    }

    @Override
    public void run() {

    }

    public Priority getPriority() {
        return priority;
    }
}