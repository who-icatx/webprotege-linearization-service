package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedissonConfiguration {

    @Value("${webprotege.redis.address}")
    private String redisAddress;

    @Value("${webprotege.redis.waitTime}")
    private int waitTime;

    @Value("${webprotege.redis.leaseTimeseTime}")
    private int leaseTime;

    @Value("${webprotege.redis.maxRetries}")
    private int maxRetries;

    @Value("${webprotege.redis.retryDelay}")
    private int retryDelay;

    public String getRedisAddress() {
        return redisAddress;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public int getLeaseTime() {
        return leaseTime;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getRetryDelay() {
        return retryDelay;
    }
}
