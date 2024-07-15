package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.config.RedissonConfiguration;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class RedissonService {

    private final RedissonClient redissonClient;
    private final RedissonConfiguration redisConfig;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public RedissonService(RedissonClient redissonClient, RedissonConfiguration redisConfig) {
        this.redissonClient = redissonClient;
        this.redisConfig = redisConfig;
    }

    public <T> T executeWithLock(String lockKey, Callable<T> callable) throws Exception {
        int maxRetries = redisConfig.getMaxRetries();
        int waitTime = redisConfig.getWaitTime();
        int leaseTime = redisConfig.getLeaseTime();
        int retryDelay = redisConfig.getRetryDelay();
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
                if (isLocked) {
                    // Execute the provided method
                    return callable.call();
                } else {
                    attempt++;
                    if (attempt < maxRetries) {
                        scheduler.schedule(() -> {
                        }, retryDelay, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while trying to acquire lock", e);
            } finally {
                if (isLocked) {
                    lock.unlock();
                }
            }
        }
        throw new RuntimeException("Could not acquire lock after " + maxRetries + " attempts");
    }

    public void executeWithLock(String lockKey, Runnable runnable) {
        int maxRetries = redisConfig.getMaxRetries();
        int waitTime = redisConfig.getWaitTime();
        int leaseTime = redisConfig.getLeaseTime();
        int retryDelay = redisConfig.getRetryDelay();
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
                if (isLocked) {
                    // Execute the provided method
                    runnable.run();
                    return;
                } else {
                    attempt++;
                    if (attempt < maxRetries) {
                        scheduler.schedule(() -> {
                        }, retryDelay, TimeUnit.MILLISECONDS);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while trying to acquire lock", e);
            } finally {
                if (isLocked) {
                    lock.unlock();
                }
            }
        }
        throw new RuntimeException("Could not acquire lock after " + maxRetries + " attempts");
    }
}
