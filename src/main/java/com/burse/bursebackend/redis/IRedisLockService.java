package com.burse.bursebackend.redis;

public interface IRedisLockService {
    boolean tryAcquireLock(String key);

    void unlock(String... keys);
}
