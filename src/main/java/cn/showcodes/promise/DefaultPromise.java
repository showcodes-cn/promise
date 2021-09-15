package cn.showcodes.promise;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultPromise<T, J> extends AbstractPromise<T, J>{
    AtomicReference<DefaultPromise> thenPromise = new AtomicReference<DefaultPromise>(null);
    T result;
    J exception;

    Function onThenResolve;
    Function onThenFail;

    DefaultPromise() {}

    DefaultPromise(BiConsumer<Consumer<T>, Consumer<J>> executor) {
        executor.accept(this::$resolve, this::$reject);
    }

    void $resolve(T result) {
        if (status.compareAndSet(PromiseStatus.pending, PromiseStatus.fulfilled)) {
            this.result = result;
            if (this.onThenResolve!=null) {
                Object ret = this.onThenResolve.apply(result);
                andThen(thenPromise.get(), ret);
            }
        }
    }

    void $reject(J exception) {
        if (status.compareAndSet(PromiseStatus.pending, PromiseStatus.rejected)) {
            this.exception = exception instanceof DefaultPromise ? (J) ((DefaultPromise<?, ?>) exception).exception : exception;
            if (this.onThenFail != null) {
                andThen(thenPromise.get(), this.onThenFail.apply(exception));
            }
        }
    }

    void andThen(AbstractPromise promise, Object ret) {
        if (ret instanceof DefaultPromise) {
            switch (((DefaultPromise<?, ?>) ret).status.get()) {
                case fulfilled:
                    promise.$resolve(((DefaultPromise<?, ?>) ret).result);
                    break;
                case rejected:
                    promise.$reject(((DefaultPromise<?, ?>) ret).exception);
                    break;
                default:
                case pending:
                    throw new IllegalStateException();
            }
        } else {
            promise.$resolve(ret);
        }
    }

    @Override
    public <P, Q> Promise<P, Q> then(Function<T, P> onResolve, Function<J, Q> onReject) {
        if (thenPromise.compareAndSet(null, new DefaultPromise())) {
            DefaultPromise p = thenPromise.get();
            switch (status.get()) {
                case fulfilled:
                    p.$resolve(onResolve == null ? result : onResolve.apply(result));
                    break;
                case rejected:
                    p.$reject(onReject == null ? exception : onReject.apply(exception));
                    break;
                default:
                    onThenResolve = onResolve;
                    onThenFail = onReject;
                    break;
            }
            return p;
        } else {
            return thenPromise.get().then(onResolve, onReject);
        }
    }
}
