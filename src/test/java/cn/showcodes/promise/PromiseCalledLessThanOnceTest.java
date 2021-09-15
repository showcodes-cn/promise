package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class PromiseCalledLessThanOnceTest extends PromiseTestbase {

    @Test
    public void alreadyFullFill() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger();
        Promise.resolve("done").then((v) -> {
            countDownLatch.countDown();
            Assert.assertEquals(1, counter.incrementAndGet());
        }, (e) -> {
            Assert.assertTrue(false);
        });
        countDownLatch.await();
    }

    @Test
    public void fullfillImmediately() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch c1 = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger();

        p.then((v) -> {
            c1.countDown();
            Assert.assertEquals(1, counter.incrementAndGet());
        }, (e) -> {
            Assert.assertTrue(false);
        });
        p.$resolve("done");
        p.$reject("fail");
        c1.await();
    }

    @Test
    public void delayed() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch c1 = new CountDownLatch(2);
        AtomicInteger counter = new AtomicInteger();

        p.then((v) -> {
            c1.countDown();
            Assert.assertEquals(1, counter.incrementAndGet());
        }, (e) -> {
            Assert.assertTrue(false);
        });

        schedule(() -> {
            p.$resolve("done");
            p.$reject("fail");
            c1.countDown();
        }, 50);

        c1.await();
    }

    @Test
    public void immediatelyThenDelayed() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        CountDownLatch c1 = new CountDownLatch(2);
        AtomicInteger counter = new AtomicInteger();

        p.then((v) -> {
            c1.countDown();
            Assert.assertEquals(1, counter.incrementAndGet());
        }, (e) -> {
            Assert.assertTrue(false);
        });

        p.$resolve("done");

        schedule(() -> {
            p.$reject("fail");
            c1.countDown();
        }, 50);

        c1.await();
    }

    @Test
    public void multipleThenCalled() throws InterruptedException {
        DefaultPromise p = new DefaultPromise();
        AtomicInteger counter = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(6);

        Promise c1 = p.then((v) ->{
            countDownLatch.countDown();
            Assert.assertEquals(1, counter.incrementAndGet());
        }, (e) -> {
            Assert.assertTrue(false);
        });

        schedule(() -> {
            countDownLatch.countDown();
            Promise c2 = p.then((v) ->{
                countDownLatch.countDown();
            }, (e) -> {});

            Assert.assertTrue(p.thenPromise == c1);
            Assert.assertTrue(((DefaultPromise) c1).thenPromise == c2);
        }, 50);

        schedule(() -> {
            countDownLatch.countDown();
            Promise c3 = p.then((v) -> {
                countDownLatch.countDown();
            }, (e) -> {});

            Assert.assertTrue(((DefaultPromise) ((DefaultPromise) p.thenPromise.get()).thenPromise.get()).thenPromise.get() == c3);
            }, 100);

        schedule(() -> {
            p.$resolve("done");
            countDownLatch.countDown();
        }, 150);

        countDownLatch.await();
    }

    @Test
    public void thenIsInterleavedWithFullfill() {
        DefaultPromise p = new DefaultPromise();
        AtomicInteger[] counters = new AtomicInteger[2];
        counters[0] = new AtomicInteger();
        counters[1] = new AtomicInteger();

        Promise c1 = p.then((v) -> {
            Assert.assertEquals(1, counters[0].incrementAndGet());
        }, (e) -> {});

        p.$resolve(Promise.resolve("done"));

        Promise c2 = p.then((v) -> {
            Assert.assertEquals(1, counters[1].incrementAndGet());
        }, (e) -> {});

        Assert.assertTrue(p.thenPromise.get() == c1);
        Assert.assertTrue(c1 != c2);
        Assert.assertTrue(((DefaultPromise) c1).thenPromise.get() == c2);
    }

}
