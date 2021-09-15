package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class PromiseAllTest {
    @Test
    public void test() throws InterruptedException {
        final Object obj = new Object();
        CountDownLatch countDownLatch = new CountDownLatch(2);
       Promise.all(
                  Promise.timeout(150).then((Consumer) null, (e) -> 1),
                  Promise.timeout(200).then((Consumer) null, (e) -> "123"),
                Promise.timeout(100).then((Consumer) null, (e) -> {
                    return obj;
                })

        ).then((v) -> {
            Assert.assertEquals(v.getClass(), new Object[0].getClass());
            Object[] objs = (Object[]) v;
            Assert.assertEquals(1, objs[0]);
           Assert.assertEquals("123", objs[1]);
           Assert.assertEquals(obj, objs[2]);
           countDownLatch.countDown();
       }, (Consumer) null);

       Promise.all(Arrays.asList(
               Promise.timeout(150).then((Consumer) null, (e) -> 1),
               Promise.timeout(200).then((Consumer) null, (e) -> "123"),
               Promise.timeout(100).then((Consumer) null, (e) -> {
                   return obj;
               }),
               Promise.timeout(250)
               )
               ).then((v) -> {
                   Assert.assertTrue(false);
       }, (e) -> {
                   Assert.assertTrue(true);
                   countDownLatch.countDown();
       });

       countDownLatch.await();
    }
}
