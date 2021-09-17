Java implementation of Promise A+

github: https://github.com/showcodes-cn/promise

|  | Status |
| :--- | :--- |
| __Build__ | [![build](https://github.com/showcodes-cn/promise/actions/workflows/maven.yml/badge.svg)](https://github.com/showocdes-cn/promise/actions/workflows/maven.yml) |
| __Code QL__ | ![CodeQL](https://github.com/showcodes-cn/promise/actions/workflows/codeql-analysis.yml/badge.svg) |
| __Test Coverage__ | ![Coverage](.github/badges/jacoco.svg) ![Branches](.github/badges/branches.svg)|



# usage
```xml
<dependency>
    <groupId>cn.showcodes</groupId>
    <artifactId>promise</artifactId>
    <version>1.0.1</version>
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