package edu.stanford.protege.webprotege.initialrevisionhistoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ReadWriteLockConfig {

    @Value("${webprotege.readWriteLock.maxRetries}")
    private int maxRetries;

    @Value("${webprotege.readWriteLock.timeoutInMillies}")
    private int timeout;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
