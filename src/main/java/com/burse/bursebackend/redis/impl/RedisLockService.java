package com.burse.bursebackend.redis.impl;

import com.burse.bursebackend.redis.IRedisLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService implements IRedisLockService {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryAcquireLock(String key) {
        RLock lock = redissonClient.getLock(key);
        try {
            boolean success=  lock.tryLock(0, TimeUnit.SECONDS);
            if (success) {
                log.debug("Acquired lock for key: {}", key);
            }
            else {
                log.debug("Failed to acquire lock for key: {}", key);
            }
            return success;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String... keys) {
        for (String key : keys) {
            log.debug("Unlocking key: {}", key);
            RLock lock = redissonClient.getLock(key);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }



}


