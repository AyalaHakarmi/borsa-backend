package com.burse.bursebackend.locks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final RedissonClient redissonClient;

    private final long leaseSeconds = 20;

    public boolean tryAcquireLock(String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(0, leaseSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public void unlock(String... keys) {
        for (String key : keys) {
            log.debug("Unlocking key: {}", key);
            redissonClient.getLock(key).unlock();
        }
    }



}


