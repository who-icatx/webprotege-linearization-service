package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@WebProtegeHandler
public class SaveEntityLinearizationCommandHandler implements CommandHandler<SaveEntityLinearizationRequest, SaveEntityLinearizationResponse> {


    private final LinearizationHistoryService linearizationHistoryService;


    public SaveEntityLinearizationCommandHandler(LinearizationHistoryService linearizationHistoryService) {
        this.linearizationHistoryService = linearizationHistoryService;
    }


    @NotNull
    @Override
    public String getChannelName() {
        return SaveEntityLinearizationRequest.CHANNEL;
    }

    @Override
    public Class<SaveEntityLinearizationRequest> getRequestClass() {
        return SaveEntityLinearizationRequest.class;
    }

    @Override
    public Mono<SaveEntityLinearizationResponse> handleRequest(SaveEntityLinearizationRequest request, ExecutionContext executionContext) {

        linearizationHistoryService.addRevision(request.entityLinearization(),executionContext, request.projectId(), executionContext.userId(), request.changeRequestId(), request.commitMessage());


        return Mono.just(SaveEntityLinearizationResponse.create());
    }
}
