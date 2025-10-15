package edu.stanford.protege.webprotege.linearizationservice.handlers;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import edu.stanford.protege.webprotege.linearizationservice.MinioLinearizationDocumentLoader;
import edu.stanford.protege.webprotege.linearizationservice.StreamUtils;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.repositories.document.LinearizationDocumentRepository;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import edu.stanford.protege.webprotege.linearizationservice.services.ReadWriteLockService;
import reactor.core.publisher.Mono;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@WebProtegeHandler
public class UploadLinearizationCommandHandler implements CommandHandler<UploadLinearizationRequest, UploadLinearizationResponse> {

    private final Logger logger = LoggerFactory.getLogger(UploadLinearizationCommandHandler.class);

    private final LinearizationDocumentRepository linearizationRepository;

    private final MinioLinearizationDocumentLoader minioDocumentLoader;

    @Value("${webprotege.linearization.batch-size:1500}")
    private int batchSize;

    private final LinearizationHistoryService linearizationHistoryService;

    private final ReadWriteLockService readWriteLock;

    private final Executor taskExecutor;

    public UploadLinearizationCommandHandler(LinearizationDocumentRepository linearizationRepository,
                                             MinioLinearizationDocumentLoader minioDocumentLoader,
                                             LinearizationHistoryService linearizationHistoryService,
                                             ReadWriteLockService readWriteLock,
                                             @Qualifier("taskExecutor") Executor taskExecutor) {
        this.linearizationRepository = linearizationRepository;
        this.minioDocumentLoader = minioDocumentLoader;
        this.linearizationHistoryService = linearizationHistoryService;
        this.readWriteLock = readWriteLock;
        this.taskExecutor = taskExecutor;
        // TODO: Implement read/write lock usage if needed for concurrent access control
    }

    @Override
    public String getChannelName() {
        return UploadLinearizationRequest.CHANNEL;
    }

    @Override
    public Class<UploadLinearizationRequest> getRequestClass() {
        return UploadLinearizationRequest.class;
    }

    @Override
    public Mono<UploadLinearizationResponse> handleRequest(UploadLinearizationRequest request,
                                                           ExecutionContext executionContext) {

        String documentId = request.documentId().id();
        
        logger.info("Starting async processing for document: {}", documentId);
        
        CompletableFuture.runAsync(() -> {
            Path tempFile = null;
            try {
                // Pasul 1: Descarcă fișierul din MinIO în fișier temporar local
                logger.info("Downloading document {} from MinIO to local file", documentId);
                tempFile = minioDocumentLoader.downloadToLocalFile(documentId);
                
                // Pasul 2: Procesează fișierul local (fără probleme de timeout)
                logger.info("Processing local file: {}", tempFile);
                var stream = linearizationRepository.fetchFromLocalFile(tempFile);
                
                Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor = 
                    linearizationHistoryService.createBatchProcessorForSavingPaginatedHistories(request.projectId(), executionContext.userId());
                
                // Procesează stream-ul din fișierul local
                stream.collect(StreamUtils.batchCollector(batchSize, batchProcessor));
                
                logger.info("Successfully processed linearization document: {}", documentId);
                
            } catch (Exception e) {
                logger.error("Error processing linearization document: {} - {}", documentId, e.getMessage(), e);
                throw e;
            } finally {
                if (tempFile != null) {
                    try {
                        minioDocumentLoader.cleanupTempFile(tempFile);
                        logger.debug("Cleaned up temporary file: {}", tempFile);
                    } catch (Exception cleanupException) {
                        logger.warn("Failed to cleanup temporary file: {} - {}", tempFile, cleanupException.getMessage());
                    }
                }
            }
        }, taskExecutor)
        .exceptionally(throwable -> {
            logger.error("Async processing failed for document: {} - {}", documentId, throwable.getMessage(), throwable);
            return null;
        });
        
        // Returnează imediat răspunsul pentru a evita timeout-ul RabbitMQ
        return Mono.just(UploadLinearizationResponse.create());
    }


}
