package cn.showcodes.promise;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Promise<T, J> {

    /**
     * create promise implementation of PromiseA+
     * @param executor
     * @param <T>
     * @param <J>
     * @return
     */
    static <T, J> Promise<T, J> create(BiConsumer<Consumer<T>, Consumer<J>> executor) {
        return new DefaultPromise(executor);
    }

    /**
     * then support
     * @param onResolve
     * @param onReject
     * @param <P>
     * @param <Q>
     * @return
     */
    <P, Q> Promise<P,Q> then(Function<T, P> onResolve, Function<J, Q> onReject);

    /**
     * Syntactic sugar
     * promise.then((v) -> {
     *     // consume v
     * }, null);
     * @param onResolve
     * @param onReject
     * @param <Q>
     * @return
     */
    default <Q> Promise<T, Q> then(Consumer<T> onResolve, Function<J, Q> onReject) {
        return then(onResolve == null ? null : t -> {
            onResolve.accept(t);
            return null;
        }, onReject);
    }

    /**
     * Syntactic sugar
     * promise.then(null, (e) -> {
     *     // do something
     * });
     * @param onResolve
     * @param onReject
     * @param <P>
     * @return
     */
    default <P> Promise<P, J> then(Function<T, P> onResolve, Consumer<J> onReject) {
        return then(onResolve, onReject == null ?  null : (e) -> {
            onReject.accept(e);
            return null;
        });
    }

    /**
     * Syntactic sugar
     * @param onResolve
     * @param onReject
     * @return
     */
    default Promise then(Consumer<T> onResolve, Consumer<J> onReject) {
        return then(onResolve == null ? null : (v) -> {
            onResolve.accept(v);
            return null;
        }, onReject == null ? null : (e) -> {
            onReject.accept(e);
            return null;
        });
    }

    /**
     * create a resolved promise with given value
     * @param obj
     * @param <T>
     * @param <J>
     * @return
     */
    static <T, J> Promise resolve(T obj) {
        return create((resolve, reject) -> {
            resolve.accept(obj);
        });
    }

    /**
     * create a rejected promise with given value
     * @param e
     * @param <J>
     * @return
     */
    static <J> Promise<Object, J> reject(J e) {
        return create((resolve, reject) -> {
            reject.accept(e);
        });
    }

    /**
     * Syntactic sugar
     * @param promises
     * @return
     */
    static Promise any(List<Promise> promises) {
        return any(promises.toArray(new Promise[0]));
    }

    /**
     * resolve or reject if any promise settled
     * @param promises
     * @return
     */
    static Promise any(Promise... promises) {
        return create((resolve, reject) -> {
            AtomicInteger total = new AtomicInteger(promises.length);
            Object[] exceptions = new Object[promises.length];

            for(int i = 0; i < promises.length; i++) {
                Promise p = promises[i];
                final int idx = i;
                p.then((v) -> {
                    resolve.accept(v);
                }, (e) -> {
                    exceptions[idx] = e;
                    if (total.decrementAndGet() == 0) {
                        reject.accept(exceptions);
                    }
                });
            }
        });
    }

    /**
     * Syntactic sugar
     * @param promises
     * @return
     */
    static Promise all(List<Promise> promises) {
        return all(promises.toArray(new Promise[0]));
    }

    /**
     * resolve only if all promises resolved
     * @param promises
     * @return
     */
    static Promise all(Promise... promises) {
        return create((resolve, reject) -> {
            AtomicInteger total = new AtomicInteger(promises.length);
            Object[] results = new Object[promises.length];

            for(int i = 0; i < promises.length; i++) {
                Promise p = promises[i];
                final int idx = i;

                p.then((v) -> {
                    results[idx] = v;
                    if (total.decrementAndGet() == 0) {
                        resolve.accept(results);
                    }
                }, (e) -> {
                    reject.accept(e);
                });
            }
        });
    }

    /**
     * Syntactic sugar
     * @param promises
     * @return
     */
    static Promise allSettled(List<Promise> promises) {
        return allSettled(promises.toArray(new Promise[0]));
    }

    /**
     * return a promise which will resolve when all promises settled(resolved or rejected)
     * which means this special promise will never trigger onReject callback
     * @param promises
     * @return
     */
    static Promise allSettled(Promise... promises) {
        return create((resolve, reject) -> {
            AtomicInteger total = new AtomicInteger(promises.length);
            Object[] results = new Object[promises.length];

            for(int i = 0; i < promises.length; i++) {
                Promise p = promises[i];
                final int idx = i;

                p.then((v) -> {
                    results[idx] = v;
                    if (total.decrementAndGet() == 0) {
                        resolve.accept(results);
                    }
                }, (e) -> {
                    results[idx] = e;
                    if (total.decrementAndGet() == 0) {
                        resolve.accept(results);
                    }
                });
            }
        });
    }

    /**
     * return a promise which will resolve or reject once any of promises settled
     * @param promises
     * @return
     */
    static Promise race(Promise... promises) {
        return create((resolve, reject) -> {
            for(int i = 0; i < promises.length; i++) {
                Promise p = promises[i];
                p.then((v) -> {
                    resolve.accept(v);
                }, (e) -> {
                    reject.accept(e);
                });
            }
        });
    }

    /**
     * create a promise will rejct TimeoutException in given timeout milliseconds
     * @param timeout
     * @return
     */
    static Promise timeout(long timeout) {
        return create((resolve, reject) -> {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    reject.accept(new TimeoutException());
                }
            }, timeout);
        });
    }

    /**
     * Syntactic sugar, keep retrying
     * @param supplier
     * @return
     */
    static Promise retry(Supplier<Promise> supplier) {
        return retry(supplier, Integer.MAX_VALUE);
    }

    /**
     * create a promise which keep retrying with given times.
     * It will reject with IllegalStateException if remain reduce to 0
     * Any resolved value makes this promise resolved
     * @param supplier
     * @param remain
     * @return
     */
    static Promise retry(Supplier<Promise> supplier, int remain) {
        return Promise.create((resolve, reject) -> {
            final AtomicInteger val = new AtomicInteger(remain);
            AtomicReference<Consumer> onRejectReference = new AtomicReference<>();
            Consumer onReject = (e) -> {
                if (val.get() <= 0) {
                    reject.accept(new IllegalStateException("too many retries"));
                } else {
                    val.decrementAndGet();
                    supplier.get().then(resolve, onRejectReference.get());
                }
            };
            onRejectReference.set(onReject);
            onReject.accept(null);
        });
    }
}
