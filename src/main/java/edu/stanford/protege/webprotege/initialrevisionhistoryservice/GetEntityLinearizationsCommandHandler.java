package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import reactor.core.publisher.Mono;

import static com.google.common.base.Preconditions.checkNotNull;


public class GetEntityLinearizationsCommandHandler implements CommandHandler<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> {


    private final LinearizationRevisionService linearizationRevisionService;


    private final LinearizationEventsProcessor linearizationEventsProcessor;

    public GetEntityLinearizationsCommandHandler(LinearizationRevisionService linearizationRevisionService,
                                                 LinearizationEventsProcessor linearizationEventsProcessor) {
        this.linearizationRevisionService = checkNotNull(linearizationRevisionService);
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
        /*
        am iri și project id
        vreau sa le iau din baza de date și să le pun într-un LinearizationChangeManager și să le aplic
         */
        EntityLinearizationHistory linearizationHistory = this.linearizationRevisionService.getExistingHistory(request.entityIRI(), request.projectId());

        WhoficEntityLinearizationSpecification processedSpec = linearizationEventsProcessor.processHistory(linearizationHistory);

        return Mono.just(new GetEntityLinearizationsResponse(processedSpec.entityIRI().toString(),processedSpec));
    }
}
