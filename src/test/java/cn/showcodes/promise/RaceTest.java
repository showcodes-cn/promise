package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class RaceTest {
    @Test
    public void race() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);

        Promise.race(Promise.timeout(5), Promise.resolve("done"))
                .then((v) -> {
                    countDownLatch.countDown();
                    Assert.assertEquals("done", v);
                }, (e) -> {
                    Assert.assertTrue(false);
                });

        Promise.race(Promise.timeout(200), Promise.create((resolve, reject) -> {
            Promise.timeout(150).then((Consumer) null, (e) -> {
                resolve.accept("waitMore");
            });
        })).then((v) -> {
            Assert.assertEquals("waitMore", v);
            countDownLatch.countDown();
        }, (e) -> {
            Assert.assertTrue(false);
        });

        countDownLatch.await();
    }
}
