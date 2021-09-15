package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class PromiseRetryTest {
    @Test
    public void test() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        AtomicInteger counter = new AtomicInteger();
        Promise.retry(() -> {
            if (counter.incrementAndGet() < 3) {
                return Promise.reject(counter.get());
            }
            return Promise.resolve(counter.get());
        }).then(v -> {
            Assert.assertEquals(3, v);
            countDownLatch.countDown();
        }, (Consumer) null);


        AtomicInteger c2 = new AtomicInteger();
        Promise.retry(() -> {
            if (c2.incrementAndGet() < 3) {
                return Promise.reject(c2.get());
            }
            return Promise.resolve(c2.get());
        }, 2).then(v -> {
            Assert.assertTrue(false);
        }, (e) -> {
            Assert.assertNotNull(e);
            Assert.assertEquals(e.getClass(), IllegalStateException.class);
            countDownLatch.countDown();
        });

        countDownLatch.await();
    }
}
