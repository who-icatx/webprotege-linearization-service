package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.mappers.WhoficEntityLinearizationSpecificationMapper;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@WebProtegeHandler
public class CreateLinearizationFromParentCommandHandler implements CommandHandler<CreateLinearizationFromParentRequest, CreateLinearizationFromParentResponse> {

    private final LinearizationHistoryService linearizationHistoryService;
    private final LinearizationEventsProcessorService linearizationEventsProcessor;
    private final WhoficEntityLinearizationSpecificationMapper whoficSpecMapper;


    public CreateLinearizationFromParentCommandHandler(LinearizationHistoryService linearizationHistoryService,
                                                       LinearizationEventsProcessorService linearizationEventsProcessor,
                                                       WhoficEntityLinearizationSpecificationMapper whoficSpecMapper) {
        this.linearizationHistoryService = linearizationHistoryService;
        this.linearizationEventsProcessor = linearizationEventsProcessor;
        this.whoficSpecMapper = whoficSpecMapper;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<CreateLinearizationFromParentRequest> getRequestClass() {
        return CreateLinearizationFromParentRequest.class;
    }

    @Override
    public Mono<CreateLinearizationFromParentResponse> handleRequest(CreateLinearizationFromParentRequest request, ExecutionContext executionContext) {
        var currentEntityHistory = linearizationHistoryService.getExistingHistoryOrderedByRevision(request.newEntityIri(), request.projectId());
        if(currentEntityHistory.isPresent()){
            return Mono.just(CreateLinearizationFromParentResponse.create());
        }

        var parentEntityHistory = linearizationHistoryService.getExistingHistoryOrderedByRevision(request.parentEntityIri(), request.projectId());
        if (parentEntityHistory.isEmpty()) {
            throw new RuntimeException("Parent entity history is empty!");
        }
        var parentWhoficSpec = linearizationEventsProcessor.processHistory(parentEntityHistory.get());
        var newEntityWhoficSpec = whoficSpecMapper.mapToDefaultWhoficEntityLinearizationSpecification(request.newEntityIri(), parentWhoficSpec);

        linearizationHistoryService.addRevision(newEntityWhoficSpec, request.projectId(), executionContext.userId());


        return Mono.just(CreateLinearizationFromParentResponse.create());
    }
}
