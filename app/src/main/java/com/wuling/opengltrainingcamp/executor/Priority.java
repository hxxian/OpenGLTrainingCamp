package com.wuling.opengltrainingcamp.executor;

/**
 * @Author: huang xiao xian
 * @Date: 2020/7/17
 * @Des: 线程任务优先级枚举
 */
public enum Priority {

    /**
     * 低优先级，可用于当前生命周期不关心的处理
     */
    LOW,

    /**
     * 中优先级，可用于接下来或者下个页面等要展示的数据
     */
    MEDIUM,

    /**
     * 高优先级，可用于当前页面需要展示的数据处理
     */
    HIGH,

    /**
     * 最高优先级，用于立刻执行的紧急处理任务
     */
    IMMEDIATE;

}
