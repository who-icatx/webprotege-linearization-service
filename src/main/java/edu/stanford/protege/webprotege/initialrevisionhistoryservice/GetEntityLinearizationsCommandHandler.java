package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import reactor.core.publisher.Mono;

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

        EntityLinearizationHistory linearizationHistory = this.linearizationHistoryService.getExistingHistoryOrderedByRevision(IRI.create(request.entityIRI()), request.projectId());

        WhoficEntityLinearizationSpecification processedSpec = linearizationEventsProcessor.processHistory(linearizationHistory);

        return Mono.just(new GetEntityLinearizationsResponse(request.entityIRI(), processedSpec));
    }
}
