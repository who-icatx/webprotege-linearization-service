package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.MergeWithParentEntitiesRequest.CHANNEL;

@WebProtegeHandler
public class MergeWithParentEntitiesCommandHandler implements CommandHandler<MergeWithParentEntitiesRequest, MergeWithParentEntitiesResponse> {

    private final LinearizationHistoryService linearizationHistoryService;
    private final LinearizationHistoryProcessorService historyProcessorService;
    private final ReadWriteLockService readWriteLock;


    public MergeWithParentEntitiesCommandHandler(LinearizationHistoryService linearizationHistoryService,
                                                 LinearizationHistoryProcessorService historyProcessorService,
                                                 ReadWriteLockService readWriteLock) {
        this.linearizationHistoryService = linearizationHistoryService;
        this.historyProcessorService = historyProcessorService;
        this.readWriteLock = readWriteLock;
    }


    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<MergeWithParentEntitiesRequest> getRequestClass() {
        return MergeWithParentEntitiesRequest.class;
    }

    @Override
    public Mono<MergeWithParentEntitiesResponse> handleRequest(MergeWithParentEntitiesRequest request, ExecutionContext executionContext) {

        readWriteLock.executeWriteLock(() -> {
            Optional<WhoficEntityLinearizationSpecification> newWhoficSpec = historyProcessorService.mergeLinearizationViewsFromParentsAndGetDefaultSpec(request.currentEntityIri(), request.parentEntityIris(), request.projectId());

            newWhoficSpec.ifPresent(newSpec -> linearizationHistoryService.addRevision(newSpec, request.projectId(), executionContext.userId()));
        });
        return Mono.just(MergeWithParentEntitiesResponse.create());
    }
}
