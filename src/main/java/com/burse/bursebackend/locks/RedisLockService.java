package com.burse.bursebackend.locks;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisLockService implements IRedisLockService {

    private final RedissonClient redissonClient;


    @Override
    public void lock(String key) {
        redissonClient.getBucket(key).setIfAbsent("locked");
    }

    @Override
    public boolean failLock(String key) {
        return !redissonClient.getBucket(key).setIfAbsent("locked");
    }

    @Override
    public void unlock(String... keys) {
        for (String key : keys) {
            redissonClient.getBucket(key).delete();
        }
    }


    @Override
    public boolean isLocked(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    @Override
    public void lockMeta(String key) {
        redissonClient.getLock(key).lock();
    }

    @Override
    public void unlockMeta(String key) {
        redissonClient.getLock(key).unlock();
    }


}


