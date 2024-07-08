package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.BlobLocation;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@WebProtegeHandler
public class UploadLinearizationCommandHandler implements CommandHandler<UploadLinearizationRequest, UploadLinearizationResponse> {

    private String bucket = "webprotege-uploads";

    private final LinearizationDocumentRepository linearizationRepository;

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

        var stream = linearizationRepository.fetchFromDocument(new BlobLocation(request.documentLocation(), this.bucket));
        int batchSize = 100;

        Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor = page -> {
            var historiesToBeSaved = page.stream()
                    .map(specification -> linearizationRevisionService.addNewRevisionToNewHistory(specification, request.projectId(), executionContext.userId().id()))
                    .collect(Collectors.toSet());

            linearizationRevisionService.saveAll(historiesToBeSaved);
        };

        stream.collect(StreamUtils.batchCollector(batchSize, batchProcessor));


        return Mono.empty();
    }


}
