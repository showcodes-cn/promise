package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class PromiseSettledTest {
    @Test
    public void test() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        List<Promise> ps = Arrays.asList(
                Promise.timeout(200),
                Promise.resolve("123"),
                Promise.retry(() -> Promise.timeout(100), 3)
        );

        Promise.allSettled(ps).then((v) -> {
            Assert.assertNotNull(v);
            Assert.assertEquals(v.getClass(), new Object[0].getClass());
            Object[] results = (Object[])v;

            Assert.assertTrue(((DefaultPromise) ps.get(0)).exception == results[0]);
            Assert.assertTrue(((DefaultPromise) ps.get(1)).result == results[1]);
            Assert.assertEquals("123", results[1]);
            Assert.assertTrue(((DefaultPromise) ps.get(2)).exception == results[2]);

            countDownLatch.countDown();
        }, (e) -> {
            Assert.assertTrue(false);
        } );

        Promise.allSettled(Promise.resolve("done"), Promise.resolve("123")).then((v) -> {
            Assert.assertNotNull(v);
            countDownLatch.countDown();
        }, (Consumer) null);

        countDownLatch.await();
    }
}
