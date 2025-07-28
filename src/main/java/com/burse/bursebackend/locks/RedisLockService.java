package com.burse.bursebackend.locks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final RedissonClient redissonClient;


    public void lock(String key) {
        log.debug("Acquiring lock for key: {}", key);
        redissonClient.getBucket(key).setIfAbsent("locked");
    }

    public boolean failLock(String key) {
        return !redissonClient.getBucket(key).setIfAbsent("locked");
    }

    public void unlock(String... keys) {
        for (String key : keys) {
            log.debug("Unlocking key: {}", key);
            redissonClient.getBucket(key).delete();
        }
    }

    public boolean isLocked(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public void lockMeta(String key) {
        log.debug("Acquiring meta lock for key: {}", key);
        redissonClient.getLock(key).lock();
    }

    public void unlockMeta(String key) {
    log.debug("Unlocking meta lock for key: {}", key);
        redissonClient.getLock(key).unlock();
    }


}


