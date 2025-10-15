package edu.stanford.protege.webprotege.linearizationservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@Component
public class MinioLinearizationDocumentLoader {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    public MinioLinearizationDocumentLoader(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public InputStream fetchLinearizationDocument(@Nonnull String location) throws StorageException {
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount <= minioProperties.getMaxRetries()) {
            try {
                return minioClient.getObject(GetObjectArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .object(location)
                        .build());
            } catch (ErrorResponseException | XmlParserException | ServerException | NoSuchAlgorithmException |
                     InvalidResponseException | InvalidKeyException | InternalException |
                     InsufficientDataException e) {
                throw new StorageException("Problem reading linearization document object from storage", e);
            } catch (IOException e) {
                lastException = e;
                
                // Check if it's a ProtocolException (connection interrupted)
                if (e instanceof ProtocolException || e.getMessage().contains("unexpected end of stream")) {
                    retryCount++;
                    if (retryCount <= minioProperties.getMaxRetries()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(minioProperties.getRetryDelayMs() * retryCount);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new StorageException("Retry interrupted", ie);
                        }
                        continue;
                    }
                }
                
                throw new StorageException("Problem reading linearization document object from storage", e);
            }
        }
        
        throw new StorageException("Failed to fetch document after " + minioProperties.getMaxRetries() + " retries", lastException);
    }
}
