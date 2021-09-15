package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Promise2_2_4Test extends PromiseTestbase {

    @Test
    public void testOnFulfillAddedInsideOnFulfilled() throws InterruptedException {
        Promise p = Promise.resolve("done");

        CountDownLatch countDownLatch = new CountDownLatch(2);
        AtomicReference<Promise> c2Reference = new AtomicReference<>();
        Promise c1 = p.then((v) -> {
            Promise c2 = p.then((v1) -> {
                countDownLatch.countDown();
            }, (e1) -> {
                Assert.assertTrue(false);
            });
            c2Reference.set(c2);

            DefaultPromise pThen = (DefaultPromise)((DefaultPromise) p).thenPromise.get();
            Assert.assertTrue(pThen.thenPromise.get() == c2);
            countDownLatch.countDown();
        }, (Consumer) null);

        Assert.assertTrue(((DefaultPromise) p).thenPromise.get() == c1);
        countDownLatch.await();
    }

    @Test
    public void onFulfilledAddedInsideRejected() throws InterruptedException {
        Promise p1 = Promise.reject("fail");
        Promise p2 = Promise.resolve("done");

        CountDownLatch countDownLatch = new CountDownLatch(2);
        p1.then((Consumer) null, (e) -> {
            countDownLatch.countDown();

            p2.then((v) -> {
                countDownLatch.countDown();
            }, (Consumer) null);
        });

        countDownLatch.await();
    }
}
