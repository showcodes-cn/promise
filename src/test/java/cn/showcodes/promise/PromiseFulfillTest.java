package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PromiseFulfillTest extends PromiseTestbase{

    @Test
    public void testFulfilled() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(4);

        testFulfilled("val", (p) -> {
            p.then((v) -> {
                Assert.assertTrue(true);
                countDownLatch.countDown();
            }, (e) -> {
                Assert.assertTrue(false);
            });
        });

        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await();
    }

    @Test
    public void testFulfillThenImmediatelyReject() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        p.then(o -> {
            Assert.assertTrue(true);
            countDownLatch.countDown();
        }, o -> {
            Assert.assertTrue(false);
        });
        p.$resolve("resolve");
        p.$reject("reject");

        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);

        countDownLatch.await();
    }

    @Test
    public void testDelayFulfillReject() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(2);

        p.then((v) -> {
            Assert.assertTrue(true);
            countDownLatch.countDown();
        }, (e) -> {
            Assert.assertTrue(false);
        });

        scheduledExecutorService.schedule(() -> {
            p.$resolve("done");
            p.$reject("fail");
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);

        countDownLatch.await();
    }

    @Test
    public void testFulfillAndThenReject() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        p.then((v) -> {
            Assert.assertTrue(true);
            countDownLatch.countDown();
        }, (e) -> {
            Assert.assertTrue(false);
        });
        p.$resolve("done");
        scheduledExecutorService.schedule(() -> {
            p.$reject("fail");
            countDownLatch.countDown();
        }, 50, TimeUnit.MILLISECONDS);
        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 100, TimeUnit.MILLISECONDS);
        countDownLatch.await();
    }

    @Test
    public void onFulfill() throws InterruptedException {
        Object val = new Object();
        CountDownLatch countDownLatch = new CountDownLatch(3);
        testFulfilled(val, (p) -> {
            p.then((v) -> {
                Assert.assertTrue(v == val);
                countDownLatch.countDown();
            }, (e) -> {
                Assert.assertTrue(false);
            });
        });
        countDownLatch.await();
    }

    @Test
    public void notBeCalledBeforeFulfill() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch countDownLatch = new CountDownLatch(2);
        p.then((v) -> {
            countDownLatch.countDown();
            Assert.assertEquals(0, countDownLatch.getCount());
        }, (e) -> {

        });

        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
            Assert.assertEquals(1, countDownLatch.getCount());
            p.$resolve("done");
        }, 50, TimeUnit.MILLISECONDS);

        countDownLatch.await();
    }

    @Test
    public void neverFulfill() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        p.then((v) -> {
            Assert.assertTrue(false);
        }, (e) ->{
            Assert.assertTrue(false);
        } );
        CountDownLatch countDownLatch = new CountDownLatch(1);
        scheduledExecutorService.schedule(() -> {
            countDownLatch.countDown();
        }, 150, TimeUnit.MILLISECONDS);
        countDownLatch.await();
    }

    @Test
    public void testThen() {
        Promise p = Promise.resolve("done");
        p.then(v -> {

        }, e -> e)
                .then((Consumer) null, (e) -> e);

        p.then(v -> v, (Consumer) null);
        Promise.reject("fail").then(v -> v, (e) -> {
            Assert.assertEquals("fail", e);
        });
    }
}
