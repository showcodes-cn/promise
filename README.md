Java implementation of Promise A+

github: https://github.com/showcodes-cn/promise

# usage
```aidl
<dependency>
    <groupId></groupId>
</dependency>
```

## basic

```java
    Promise<String, Exception> p = Promise.create((resolve, reject) -> {
        // resolve or reject
        if (some condition) {
            resolve.accept("done");
        } else {
            reject.accept(new Exception());
        }
        });

    p.then((v) -> {
        return v;
        }, (e) -> {
        return e;
        });
```

## utils

```java
    // create a resolved object
    Promise p = Promise.resolve(new Object());

    // create a rejected object
    Promise p = Promise.reject(new Object());
    
    // timeout promise
    Promise p = Promise.timeout(100);
    
    // race
    Promise p = Promise.race(
            Promise.timeout(100).then((Consume) null, (e) -> "123"),
            Promise.timeout(150)
            );
    
    // any
    Promise p = Promise.any(Promise.resolve("1"), Promise.timeout(100));
    
    // all
    Promise p = Promise.all(Promise.resolve(new Object()), Promise.reject(new Object()));
    
    // allsettled
    Promise p = Promise.allSettled(Promise.resolve(new Object()), Promise.reject(new Object()));

    // retry
    Promise p = Promise.retry(() -> Promise.timeout(100), 3);
```