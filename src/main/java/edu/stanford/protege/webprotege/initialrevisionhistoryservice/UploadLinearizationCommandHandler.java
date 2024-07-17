package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.BlobLocation;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.LinearizationHistoryService;
import edu.stanford.protege.webprotege.ipc.*;
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

    private final String bucket = "webprotege-uploads";

    private final LinearizationDocumentRepository linearizationRepository;

    @Value("${webprotege.linearization.batch-size}")
    private int batchSize;

    private final LinearizationHistoryService linearizationHistoryService;

    public UploadLinearizationCommandHandler(LinearizationDocumentRepository linearizationRepository, LinearizationHistoryService linearizationHistoryService) {
        this.linearizationRepository = linearizationRepository;
        this.linearizationHistoryService = linearizationHistoryService;
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

        var stream = linearizationRepository.fetchFromDocument(new BlobLocation(request.documentLocation(), this.bucket));

        Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor = linearizationHistoryService.createBatchProcessorForSavingPaginatedHistories(request.projectId(), executionContext.userId());

        stream.collect(StreamUtils.batchCollector(batchSize, batchProcessor));


        return Mono.just(UploadLinearizationResponse.create());
    }


}
