package cn.showcodes.promise;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractPromise<T, J> implements Promise<T, J> {
    AtomicReference<PromiseStatus> status = new AtomicReference<>(PromiseStatus.pending);
    abstract void $resolve(T result);
    abstract void $reject(J exception);
}
