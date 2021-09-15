package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PromiseMultipleThenTest extends PromiseTestbase{

    @Test
    public void test() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        AtomicInteger atomicInteger = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(4);

        p.then((v) -> {
            Assert.assertEquals(0, v);
            Assert.assertEquals(v, atomicInteger.get());
            countDownLatch.countDown();
            return Promise.resolve(atomicInteger.incrementAndGet());
        }, (Consumer) null)
                .then((v) -> {
            Assert.assertEquals(1, v);
            Assert.assertEquals(v, atomicInteger.get());
            countDownLatch.countDown();
            return Promise.reject(atomicInteger.incrementAndGet());
        }, (e) -> {
                    Assert.assertTrue(false);
                })
        .then((Consumer) null, (e) -> {

            Assert.assertEquals(2, e);
            Assert.assertEquals(e, atomicInteger.get());
            countDownLatch.countDown();

            return Promise.reject("fail").then((v) -> {
                Assert.assertTrue(false);
            }, (Consumer)null).then((Consumer) null, (e2) -> {
                Assert.assertEquals(e2, "fail");
                countDownLatch.countDown();
            });
        });

        p.$resolve(atomicInteger.get());
        countDownLatch.await();
    }
}
