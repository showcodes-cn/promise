package cn.showcodes.promise;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PromiseTestbase {

    protected ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    void testFulfilled(Object val, Consumer<Promise> callback) {
        callback.accept(Promise.resolve(val));
        DefaultPromise p = new DefaultPromise();
        callback.accept(p);
        p.$resolve(val);

        DefaultPromise later = new DefaultPromise();
        scheduledExecutorService.schedule(() -> {
            later.$resolve(val);

        }, 50, TimeUnit.MILLISECONDS);
        callback.accept(later);

    }

    void testReject(Object val, Consumer<Promise> callback) {
        callback.accept(Promise.reject(val));
        DefaultPromise p = new DefaultPromise();
        callback.accept(p);
        p.$reject(val);

        DefaultPromise later = new DefaultPromise();
        scheduledExecutorService.schedule(() -> {
            later.$reject(val);

        }, 50, TimeUnit.MILLISECONDS);
        callback.accept(later);
    }

    void schedule(Runnable runnable, long timeout) {
        scheduledExecutorService.schedule(runnable, timeout, TimeUnit.MILLISECONDS);
    }
}
