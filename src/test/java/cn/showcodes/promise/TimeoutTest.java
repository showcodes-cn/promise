package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class TimeoutTest {

    @Test
    public void test() {
        long z = 1000;
        Promise timeout = Promise.timeout(z);
        long start = System.currentTimeMillis();
        timeout.then((v) -> {
            Assert.assertTrue(false);
        }, (e) -> {
            Assert.assertNotNull(e);
            Assert.assertEquals(TimeoutException.class, e.getClass());
            Assert.assertTrue( System.currentTimeMillis() - start - z < 1);
        });
    }
}
