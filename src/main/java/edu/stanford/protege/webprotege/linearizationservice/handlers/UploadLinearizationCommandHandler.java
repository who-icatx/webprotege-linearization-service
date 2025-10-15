package edu.stanford.protege.webprotege.linearizationservice.handlers;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public UploadLinearizationCommandHandler(LinearizationDocumentRepository linearizationRepository,
                                             MinioLinearizationDocumentLoader minioDocumentLoader,
                                             LinearizationHistoryService linearizationHistoryService,
                                             ReadWriteLockService readWriteLock) {
        this.linearizationRepository = linearizationRepository;
        this.minioDocumentLoader = minioDocumentLoader;
        this.linearizationHistoryService = linearizationHistoryService;
        this.readWriteLock = readWriteLock;
        // TODO: Implement read/write lock usage if needed for concurrent access control
    }

    @NotNull
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
            return Mono.just(UploadLinearizationResponse.create());
            
        } catch (RuntimeException e) {
            logger.error("Error processing linearization document: {}", documentId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error processing linearization document: {}", documentId, e);
            throw new RuntimeException("Failed to process linearization document: " + documentId, e);
        } finally {
            // Pasul 3: Cleanup - șterge fișierul temporar
            if (tempFile != null) {
                minioDocumentLoader.cleanupTempFile(tempFile);
            }
        }
    }


}
