package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.BlobLocation;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@WebProtegeHandler
public class UploadLinearizationCommandHandler implements CommandHandler<UploadLinearizationRequest, UploadLinearizationResponse> {

    private final static Logger LOGGER = LoggerFactory.getLogger(UploadLinearizationCommandHandler.class);

    private String bucket = "webprotege-uploads";

    private final LinearizationDocumentRepository linearizationRepository;

    @Value("${webprotege.linearization.batch-size:500}")
    private int batchSize;

    private final LinearizationRevisionService linearizationRevisionService;

    public UploadLinearizationCommandHandler(LinearizationDocumentRepository linearizationRepository, LinearizationRevisionService linearizationRevisionService) {
        this.linearizationRepository = linearizationRepository;
        this.linearizationRevisionService = linearizationRevisionService;
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

        var stream = linearizationRepository.fetchFromDocument(new BlobLocation(this.bucket, request.documentId().id()));

        Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor = page -> {
            var historiesToBeSaved = page.stream()
                    .map(specification -> linearizationRevisionService.addNewRevisionToNewHistory(specification, request.projectId(), executionContext.userId().id()))
                    .collect(Collectors.toSet());

            linearizationRevisionService.saveEntityLinearizationHistory(historiesToBeSaved);
        };

        stream.collect(StreamUtils.batchCollector(batchSize, batchProcessor));

        LOGGER.info("Finished processing request for project: {} and document : {}", request.projectId(), request.documentId());
        return Mono.just(new UploadLinearizationResponse(new BlobLocation(this.bucket, request.documentId().id())));
    }


}
