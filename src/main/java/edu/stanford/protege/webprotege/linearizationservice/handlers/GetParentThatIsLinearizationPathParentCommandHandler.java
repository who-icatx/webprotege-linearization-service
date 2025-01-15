package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationEventsProcessorService;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebProtegeHandler
public class GetParentThatIsLinearizationPathParentCommandHandler implements CommandHandler<GetParentThatIsLinearizationPathParentRequest, GetParentThatIsLinearizationPathParentResponse> {

    private final LinearizationHistoryService linearizationHistoryService;
    private final LinearizationEventsProcessorService linearizationEventsProcessor;


    public GetParentThatIsLinearizationPathParentCommandHandler(LinearizationHistoryService linearizationHistoryService,
                                                                LinearizationEventsProcessorService linearizationEventsProcessor) {
        this.linearizationHistoryService = linearizationHistoryService;
        this.linearizationEventsProcessor = linearizationEventsProcessor;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return GetParentThatIsLinearizationPathParentRequest.CHANNEL;
    }

    @Override
    public Class<GetParentThatIsLinearizationPathParentRequest> getRequestClass() {
        return GetParentThatIsLinearizationPathParentRequest.class;
    }

    @Override
    public Mono<GetParentThatIsLinearizationPathParentResponse> handleRequest(GetParentThatIsLinearizationPathParentRequest request, ExecutionContext executionContext) {

        WhoficEntityLinearizationSpecification processedSpec =
                this.linearizationHistoryService.getExistingHistoryOrderedByRevision(request.currentEntityIri(), request.projectId())
                        .map(linearizationEventsProcessor::processHistory)
                        .orElseGet(() -> new WhoficEntityLinearizationSpecification(request.currentEntityIri(), null, Collections.emptyList()));

        var linearizationParents = processedSpec.linearizationSpecifications()
                .stream()
                .map(spec -> Stream.of(spec.getLinearizationParent()))
                .collect(Collectors.toSet());
        var matchingParent = request.parentEntityIris()
                .stream()
                .filter(linearizationParents::contains)
                .findAny();

        return Mono.just(new GetParentThatIsLinearizationPathParentResponse(matchingParent));
    }
}
