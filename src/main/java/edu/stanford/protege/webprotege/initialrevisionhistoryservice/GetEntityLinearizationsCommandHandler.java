package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;


@WebProtegeHandler
public class GetEntityLinearizationsCommandHandler implements CommandHandler<GetEntityLinearizationsRequest, GetEntityLinearizationsResponse> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GetEntityLinearizationsCommandHandler.class);

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

        WhoficEntityLinearizationSpecification processedSpec =
                this.linearizationHistoryService.getExistingHistoryOrderedByRevision(request.entityIRI(), request.projectId())
                        .map(linearizationEventsProcessor::processHistory)
                        .orElseGet(() -> new WhoficEntityLinearizationSpecification(null, null, Collections.emptyList()));
        LOGGER.info("Returning processed spec {}", processedSpec);
        return Mono.just(new GetEntityLinearizationsResponse(request.entityIRI(), processedSpec));
    }
}
