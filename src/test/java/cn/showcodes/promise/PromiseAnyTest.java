package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class PromiseAnyTest {
    @Test
    public void test() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        Promise.any(
                Promise.timeout(100).then((Consumer) null, (e) -> "123"),
                Promise.timeout(50).then((Consumer) null, (e) -> "456"),
                Promise.timeout(150).then((Consumer) null, (e) -> "123")
                ).then(v -> {
            Assert.assertEquals("456", v);
            countDownLatch.countDown();
        }, (Consumer) null);

        List<Promise> ps = Arrays.asList(
                Promise.timeout(100),
                Promise.timeout(50),
                Promise.timeout(150));
        Promise.any(ps).then((Consumer) null, v -> {
            Assert.assertNotNull(v);
            Assert.assertEquals(v.getClass(), new Object[0].getClass());
            Object[] results = (Object[])v;
            Assert.assertTrue(((DefaultPromise) ps.get(0)).exception == results[0]);
            Assert.assertTrue(((DefaultPromise) ps.get(1)).exception == results[1]);
            Assert.assertTrue(((DefaultPromise) ps.get(2)).exception == results[2]);
            countDownLatch.countDown();
        });

        countDownLatch.await();
    }
}
