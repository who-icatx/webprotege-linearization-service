package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.StreamUtils;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.repositories.document.LinearizationDocumentRepository;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.ProjectLinearizationChangedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@WebProtegeHandler
public class UploadLinearizationCommandHandler implements CommandHandler<UploadLinearizationRequest, UploadLinearizationResponse> {

    private final LinearizationDocumentRepository linearizationRepository;

    @Value("${webprotege.linearization.batch-size:500}")
    private int batchSize;

    private final LinearizationHistoryService linearizationHistoryService;

    private final ReadWriteLockService readWriteLock;

    private final LinearizationChangeEmitterService linChangeEmitter;


    public UploadLinearizationCommandHandler(LinearizationDocumentRepository linearizationRepository,
                                             LinearizationHistoryService linearizationHistoryService,
                                             ReadWriteLockService readWriteLock,
                                             LinearizationChangeEmitterService linChangeEmitter) {
        this.linearizationRepository = linearizationRepository;
        this.linearizationHistoryService = linearizationHistoryService;
        this.readWriteLock = readWriteLock;
        this.linChangeEmitter = linChangeEmitter;
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

        readWriteLock.executeWriteLock(() -> {
            Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor = linearizationHistoryService.createBatchProcessorForSavingPaginatedHistories(request.projectId(), executionContext.userId());
            stream.collect(StreamUtils.batchCollector(batchSize, batchProcessor));
        });

        linChangeEmitter.emitLinearizationChangeEvent(request.projectId(), List.of(ProjectLinearizationChangedEvent.create(EventId.generate(), request.projectId())));

        return Mono.just(UploadLinearizationResponse.create());
    }


}
