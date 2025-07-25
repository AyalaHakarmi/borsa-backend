package com.burse.bursebackend.locks;

public interface IRedisLockService {
    void lock(String key);
    boolean failLock(String key);
    void unlock(String... keys);
    boolean isLocked(String key);
    void lockMeta(String key);
    void unlockMeta(String key);

}

