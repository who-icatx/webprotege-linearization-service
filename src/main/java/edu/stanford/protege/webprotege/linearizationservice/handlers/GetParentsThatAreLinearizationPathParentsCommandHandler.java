package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;

@WebProtegeHandler
public class GetParentsThatAreLinearizationPathParentsCommandHandler implements CommandHandler<GetParentsThatAreLinearizationPathParentsRequest, GetParentsThatAreLinearizationPathParentsResponse> {

    private final LinearizationHistoryService linearizationHistoryService;
    private final LinearizationEventsProcessorService linearizationEventsProcessor;


    public GetParentsThatAreLinearizationPathParentsCommandHandler(LinearizationHistoryService linearizationHistoryService,
                                                                   LinearizationEventsProcessorService linearizationEventsProcessor) {
        this.linearizationHistoryService = linearizationHistoryService;
        this.linearizationEventsProcessor = linearizationEventsProcessor;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return GetParentsThatAreLinearizationPathParentsRequest.CHANNEL;
    }

    @Override
    public Class<GetParentsThatAreLinearizationPathParentsRequest> getRequestClass() {
        return GetParentsThatAreLinearizationPathParentsRequest.class;
    }

    @Override
    public Mono<GetParentsThatAreLinearizationPathParentsResponse> handleRequest(GetParentsThatAreLinearizationPathParentsRequest request, ExecutionContext executionContext) {

        WhoficEntityLinearizationSpecification processedSpec =
                this.linearizationHistoryService.getExistingHistoryOrderedByRevision(request.currentEntityIri(), request.projectId())
                        .map(linearizationEventsProcessor::processHistory)
                        .orElseGet(() -> new WhoficEntityLinearizationSpecification(request.currentEntityIri(), null, Collections.emptyList()));

        var linearizationParents = processedSpec.linearizationSpecifications()
                .stream()
                .map(LinearizationSpecification::getLinearizationParent)
                .filter(parentIri -> parentIri!=null && !parentIri.toString().isEmpty())
                .collect(Collectors.toSet());
        var matchingParent = request.parentEntityIris()
                .stream()
                .filter(linearizationParents::contains)
                .collect(Collectors.toSet());

        return Mono.just(new GetParentsThatAreLinearizationPathParentsResponse(matchingParent));
    }
}
