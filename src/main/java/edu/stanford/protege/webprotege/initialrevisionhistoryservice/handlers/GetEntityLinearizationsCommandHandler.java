package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;


@WebProtegeHandler
public class GetEntityLinearizationsCommandHandler implements CommandHandler<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> {


    private final LinearizationHistoryService linearizationHistoryService;


    private final LinearizationEventsProcessorService linearizationEventsProcessor;

    public GetEntityLinearizationsCommandHandler(LinearizationHistoryService linearizationHistoryService,
                                                 LinearizationEventsProcessorService linearizationEventsProcessor) {
        this.linearizationHistoryService = checkNotNull(linearizationHistoryService);
        this.linearizationEventsProcessor = checkNotNull(linearizationEventsProcessor);
    }


    @NotNull
    @Override
    public String getChannelName() {
        return GetEntityLinearizationsRequest.CHANNEL;
    }

    @Override
    public Class<GetEntityLinearizationsRequest> getRequestClass() {
        return GetEntityLinearizationsRequest.class;
    }

    @Override
    public Mono<GetEntityLinearizationsResponse> handleRequest(GetEntityLinearizationsRequest request, ExecutionContext executionContext) {
        var currentEntityIri = IRI.create(request.entityIRI());

        WhoficEntityLinearizationSpecification processedSpec =
                this.linearizationHistoryService.getExistingHistoryOrderedByRevision(currentEntityIri, request.projectId())
                        .map(linearizationEventsProcessor::processHistory)
                        .orElseGet(() -> new WhoficEntityLinearizationSpecification(currentEntityIri, null, Collections.emptyList()));

        return Mono.just(new GetEntityLinearizationsResponse(request.entityIRI(), processedSpec));
    }
}
