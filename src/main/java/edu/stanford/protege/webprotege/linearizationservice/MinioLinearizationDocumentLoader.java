package edu.stanford.protege.webprotege.linearizationservice;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.*;
import java.security.*;

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
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(location)
                    .build());
        } catch (ErrorResponseException | XmlParserException | ServerException | NoSuchAlgorithmException |
                 IOException | InvalidResponseException | InvalidKeyException | InternalException |
                 InsufficientDataException e) {
            throw new StorageException("Problem reading linearization document object from storage", e);
        }
    }
}
