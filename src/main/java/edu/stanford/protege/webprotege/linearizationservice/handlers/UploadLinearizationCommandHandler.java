package edu.stanford.protege.webprotege.linearizationservice.handlers;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
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

    @Value("${webprotege.linearization.batch-size:1500}")
    private int batchSize;

    private final LinearizationHistoryService linearizationHistoryService;

    private final ReadWriteLockService readWriteLock;


    public UploadLinearizationCommandHandler(LinearizationDocumentRepository linearizationRepository,
                                             LinearizationHistoryService linearizationHistoryService,
                                             ReadWriteLockService readWriteLock) {
        this.linearizationRepository = linearizationRepository;
        this.linearizationHistoryService = linearizationHistoryService;
        this.readWriteLock = readWriteLock;
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

        var stream = linearizationRepository.fetchFromDocument(request.documentId().id());

        Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor = linearizationHistoryService.createBatchProcessorForSavingPaginatedHistories(request.projectId(), executionContext.userId());
        
        // Procesează imediat stream-ul pentru a evita lazy loading issues
        try {
            stream.collect(StreamUtils.batchCollector(batchSize, batchProcessor));
        } catch (Exception e) {
            logger.error("Error processing linearization stream for document: {}", request.documentId().id(), e);
            throw new RuntimeException("Failed to process linearization document", e);
        }


        return Mono.just(UploadLinearizationResponse.create());
    }


}
