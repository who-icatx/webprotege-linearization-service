package edu.stanford.protege.webprotege.linearizationservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(MinioLinearizationDocumentLoader.class);

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

    /**
     * Descarcă fișierul local și returnează calea către fișierul temporar
     * @param location locația fișierului în MinIO
     * @return Path către fișierul temporar descărcat
     * @throws StorageException dacă descărcarea eșuează
     */
    public Path downloadToLocalFile(@Nonnull String location) throws StorageException {
        try {
            // Creează un fișier temporar
            Path tempFile = Files.createTempFile("linearization_", ".json");
            logger.info("Downloading MinIO object {} to temporary file: {}", location, tempFile);
            
            // Descarcă fișierul din MinIO în fișierul temporar
            try (InputStream inputStream = fetchLinearizationDocument(location)) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            logger.info("Successfully downloaded {} bytes to {}", Files.size(tempFile), tempFile);
            return tempFile;
            
        } catch (IOException e) {
            logger.error("Failed to download MinIO object {} to local file", location, e);
            throw new StorageException("Failed to download document to local file: " + location, e);
        }
    }

    /**
     * Șterge fișierul temporar
     * @param tempFile calea către fișierul temporar
     */
    public void cleanupTempFile(@Nonnull Path tempFile) {
        try {
            if (Files.exists(tempFile)) {
                Files.delete(tempFile);
                logger.info("Cleaned up temporary file: {}", tempFile);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete temporary file: {}", tempFile, e);
        }
    }
}
