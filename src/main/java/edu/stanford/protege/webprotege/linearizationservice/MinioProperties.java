package edu.stanford.protege.webprotege.linearizationservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@Component
@ConfigurationProperties(prefix = "webprotege.minio")
public class MinioProperties {

    private String accessKey;

    private String secretKey;

    private String endPoint;

    private String bucketName;

    private String revisionHistoryDocumentsBucketName;

    private int connectionTimeout = 3000000; // 30 seconds
    private int readTimeout = 30000000; // 5 minutes
    private int writeTimeout = 30000000; // 5 minutes
    private int maxRetries = 3;
    private int retryDelayMs = 1000; // 1 second

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRevisionHistoryDocumentsBucketName() {
        return revisionHistoryDocumentsBucketName;
    }

    public void setRevisionHistoryDocumentsBucketName(String revisionHistoryDocumentsBucketName) {
        this.revisionHistoryDocumentsBucketName = revisionHistoryDocumentsBucketName;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(int retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
}
