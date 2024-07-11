package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static com.google.common.base.Preconditions.checkNotNull;


@WebProtegeHandler
public class GetEntityLinearizationsCommandHandler implements CommandHandler<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> {


    private final LinearizationRevisionService linearizationRevisionService;


    private final LinearizationEventsProcessorService linearizationEventsProcessor;

    public GetEntityLinearizationsCommandHandler(LinearizationRevisionService linearizationRevisionService,
                                                 LinearizationEventsProcessorService linearizationEventsProcessor) {
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

        EntityLinearizationHistory linearizationHistory = this.linearizationRevisionService.getExistingHistoryOrderedByRevision(request.entityIRI(), request.projectId());

        WhoficEntityLinearizationSpecification processedSpec = linearizationEventsProcessor.processHistory(linearizationHistory);

        return Mono.just(new GetEntityLinearizationsResponse(processedSpec.entityIRI().toString(),processedSpec));
    }
}
