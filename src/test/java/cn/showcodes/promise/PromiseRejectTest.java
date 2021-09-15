package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PromiseRejectTest extends PromiseTestbase{
    @Test
    public void testReject() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(3);

        testReject("fali", (p) -> {
            p.then((v) -> {
                Assert.assertTrue(false);
            }, (e) -> {
                Assert.assertTrue(true);
                countDownLatch.countDown();
            });
        });
        countDownLatch.await();
    }

    @Test
    public void testImmediatelyFullfill() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        p.then((v) -> {
            Assert.assertTrue(false);
        }, (e) -> {
            Assert.assertTrue(true);
            countDownLatch.countDown();
        });
        p.$reject("fail");
        p.$resolve("done");
        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await();
    }

    @Test
    public void testRejectThenFulfillDelayed() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        p.then((v) -> {
            Assert.assertTrue(false);
        }, (e) -> {
            Assert.assertTrue(true);
            countDownLatch.countDown();
        });

        scheduledExecutorService.schedule(() -> {
            p.$reject("fail");
            p.$resolve("done");
            countDownLatch.countDown();
        }, 50, TimeUnit.MILLISECONDS);

        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await();
    }

    @Test
    public void testRejectImmediatelyThenFulfillDelayed() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        p.then((v) -> {
            Assert.assertTrue(false);
        }, (e) -> {
            Assert.assertTrue(true);
            countDownLatch.countDown();
        });
        p.$reject("fail");

        scheduledExecutorService.schedule(() -> {
            p.$resolve("done");
            countDownLatch.countDown();
        }, 50, TimeUnit.MILLISECONDS);

        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await();
    }

    @Test
    public void neverReject() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        p.then((v) -> {
            Assert.assertTrue(false);
        }, (v) -> {
            Assert.assertTrue(false);
        });

        schedule(() -> {
            countDownLatch.countDown();
        }, 150);
        countDownLatch.await();
    }

}
