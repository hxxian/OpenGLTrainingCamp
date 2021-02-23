package com.wuling.opengltrainingcamp.executor;

import android.os.Process;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: huang xiao xian
 * @Date: 2020/7/17
 * @Des: 综合线程池
 *       ExecutorSupplier提供了多种用于不同场景的线程池，包括UI线程池、重度任务线程池、轻度任务线程池、
 *       定时任务线程池，以及支持优先级的线程池（但优先级不是绝对可靠，系统决定）。
 *
 *       在整个应用生命周期中，可能会同时存在非常耗时的操作和一般耗时的操作，在这种情况，如果设定的线程数
 *       固定，且只有一种线程池，那么极端的情况下，所有线程都被非常耗时的操作所占用，而一般耗时的操作，
 *       原本应该很快可以得到结果，却因为线程池的管理策略而导致任务迟迟没有被执行，这种情况有时候不是我们
 *       所希望的。所以，ExecutorSupplier定义了重度任务线程池和轻度任务线程池，重度任务和轻度任务分开管理，
 *       使用过程中可自行判断哪些任务是重度任务，哪些任务是轻度任务，然后合理正确使用线程池即可。
 *
 *       对于可感知优先级的线程池，如果你的处理里有优先级的概念，那么可以考虑把希望受到优先级控制的任务
 *       交给该线程池管理。
 *
 */
public class ExecutorSupplier {

    /**
     * 核心线程数，一般为设备CPU核数的2倍，太小不利于提高吞吐量，太大则会存在过多的线程而占用太多系统系统，
     * 也增加了线程频繁切换的开销
     */
    private static final int THREAD_CORE_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 线程空闲时的存活时间，超过这个值则销毁线程
     */
    private static final long KEEP_ALIVE_TIME = 60L;

    /**
     * 主线程UI操作任务执行器
     */
    private final MainThreadExecutor mainThreadExecutor;

    /**
     * 重度后台任务执行器，如文件读写、网络请求，注意不要与轻度任务一起使用
     */
    private final ThreadPoolExecutor backgroundTaskExecutor;

    /**
     * 轻度后台任务执行器，如数据解析、序列化操作等，注意不要与重度任务一起使用
     */
    private final ThreadPoolExecutor lightBackgroundTaskExecutor;

    /**
     * 优先级感知线程池
     */
    private final PriorityThreadPoolExecutor priorityThreadPoolExecutor;

    /**
     * 定时任务线程池
     */
    private final ScheduledExecutorService scheduledExecutorService;

    public static ExecutorSupplier getInstance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final ExecutorSupplier INSTANCE = new ExecutorSupplier();
    }

    private ExecutorSupplier() {
        ThreadFactory backgroundPriorityThreadFactory = new
                PriorityThreadFactory(Process.THREAD_PRIORITY_BACKGROUND);

        mainThreadExecutor = new MainThreadExecutor();

        backgroundTaskExecutor = new ThreadPoolExecutor(
                THREAD_CORE_SIZE,
                THREAD_CORE_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        );

        lightBackgroundTaskExecutor = new ThreadPoolExecutor(
                THREAD_CORE_SIZE,
                THREAD_CORE_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                backgroundPriorityThreadFactory
        );

        priorityThreadPoolExecutor = new PriorityThreadPoolExecutor(
                THREAD_CORE_SIZE,
                THREAD_CORE_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                backgroundPriorityThreadFactory
        );

        scheduledExecutorService = new ScheduledThreadPoolExecutor(
                THREAD_CORE_SIZE,
                backgroundPriorityThreadFactory
        );

    }

    /**
     * 执行UI任务操作
     *
     * @param task
     */
    public void executeUITask(Runnable task) {
        if (task != null) {
            mainThreadExecutor.execute(task);
        }
    }

    /**
     * 在指定延迟时间后，执行UI任务
     *
     * @param task
     * @param delay：延迟时间，单位毫秒
     */
    public void executeUITask(Runnable task, long delay) {
        if (task != null) {
            mainThreadExecutor.execute(task, delay);
        }
    }

    /**
     * 执行重度后台任务，文件读写、网络请求等任务，均调用此方法执行
     *
     * @param task
     */
    public void executeBackgroundTask(Runnable task) {
        if (task != null) {
            backgroundTaskExecutor.execute(task);
        }
    }

    /**
     * 执行轻度后台任务，如数据解析、序列化或者其他不是十分耗时但又不应该放在UI线程执行的任务
     *
     * @param task
     */
    public void executeLightBackgroundTask(Runnable task) {
        if (task != null) {
            lightBackgroundTaskExecutor.execute(task);
        }
    }

    /**
     * 执行重度后台任务，文件读写、网络请求等任务，均调用此方法执行
     * 该方法返回一个future对象，允许你中断任务
     * 即：future.cancel(true);
     *
     * @param task
     * @return
     */
    public Future executeBackgroundTaskWithFuture(Runnable task) {
        if (task == null) {
            // 抛出异常，可让你提前感知你传入非法参数
            throw new RuntimeException("不可传入空的执行任务!");
        }
        return backgroundTaskExecutor.submit(task);
    }

    /**
     * 执行轻度后台任务，如数据解析、序列化或者其他不是十分耗时但又不应该放在UI线程执行的任务
     * 该方法返回一个future对象，允许你中断任务
     * 即：future.cancel(true)
     *
     * @param task
     * @return
     */
    public Future executeLightBackgroundTaskWithFuture(Runnable task) {
        if (task == null) {
            // 抛出异常，可让你提前感知你传入非法参数
            throw new RuntimeException("不可传入空的执行任务!");
        }
        return lightBackgroundTaskExecutor.submit(task);
    }

    /**
     * 执行带有优先级的任务
     *
     * @param task
     * @return
     */
    public Future executePriorityBackgroundTask(PriorityRunnable task) {
        if (task == null) {
            // 抛出异常，可让你提前感知你传入非法参数
            throw new RuntimeException("不可传入空的执行任务!");
        }
        return priorityThreadPoolExecutor.submit(task);
    }

    /**
     * 定时执行任务，执行完一次则结束
     *
     * @param task
     * @param delay：任务开始执行延时，单位毫秒
     */
    public void executeScheduledTask(Runnable task, long delay) {
        if (task != null) {
            scheduledExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 定时执行，每隔一段时间执行任务
     *
     * @param task
     * @param delay：第一次执行延期执行时长，单位毫秒
     * @param period：间隔时长，单位毫秒
     * @return 调用者一定要拿到该返回对象，用于在合适的时机停止定时任务
     */
    public ScheduledFuture executeScheduledAtFixedRate(Runnable task, long delay, long period) {
        if (task == null) {
            // 抛出异常，可让你提前感知你传入非法参数
            throw new RuntimeException("不可传入空的执行任务!");
        }
        return scheduledExecutorService.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
    }

}