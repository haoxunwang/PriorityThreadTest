package com.nelson.prioritythreadtest;

import android.annotation.TargetApi;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nelson on 2017/12/4.
 */
public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE;
    private static final int MAXIMUM_POOL_SIZE;
    private static final long KEEP_ALIVE = 1L;

    <T extends Runnable & Dependency & Task & PriorityProvider> PriorityThreadPoolExecutor(
            int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            DependencyPriorityBlockingQueue<T> workQueue, ThreadFactory factory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
        this.prestartAllCoreThreads();
    }

    public static <T extends Runnable & Dependency & Task & PriorityProvider> PriorityThreadPoolExecutor create(
            int corePoolSize, int maxPoolSize) {
        return new PriorityThreadPoolExecutor(corePoolSize, maxPoolSize, 1L, TimeUnit.SECONDS,
                new DependencyPriorityBlockingQueue(),
                new PriorityThreadPoolExecutor.PriorityThreadFactory(10));
    }

    public static PriorityThreadPoolExecutor create(int threadCount) {
        return create(threadCount, threadCount);
    }

    public static PriorityThreadPoolExecutor create() {
        return create(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE);
    }

    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new PriorityFutureTask(runnable, value);
    }

    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new PriorityFutureTask(callable);
    }

    @TargetApi(9)
    public void execute(Runnable command) {
        if (PriorityTask.isProperDelegate(command)) {
            super.execute(command);
        } else {
            super.execute(this.newTaskFor(command, (Object) null));
        }

    }

    protected void afterExecute(Runnable runnable, Throwable throwable) {
        Task task = (Task) runnable;
        task.setFinished(true);
        task.setError(throwable);
        this.getQueue().recycleBlockedQueue();
        super.afterExecute(runnable, throwable);
    }

    public DependencyPriorityBlockingQueue getQueue() {
        return (DependencyPriorityBlockingQueue) super.getQueue();
    }

    static {
        CORE_POOL_SIZE = CPU_COUNT + 1;
        MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    }

    protected static final class PriorityThreadFactory implements ThreadFactory {

        private final int threadPriority;

        public PriorityThreadFactory(int threadPriority) {
            this.threadPriority = threadPriority;
        }

        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setPriority(this.threadPriority);
            thread.setName("Queue");
            return thread;
        }
    }
}
