package cn.showcodes.promise;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Consumer;

public class Promise2_3_1Test extends PromiseTestbase{
    @Test
    public void testFufill() {
        Promise p = Promise.resolve("done");
        p.then((v) -> p, (Consumer) null).then(v -> {
            Assert.assertEquals(v, p);
        }, (Consumer) null);
    }

    @Test
    public void testReject() {
        Promise p = Promise.reject("fail");
        p.then((Consumer) null, (e) -> p);
    }
}
