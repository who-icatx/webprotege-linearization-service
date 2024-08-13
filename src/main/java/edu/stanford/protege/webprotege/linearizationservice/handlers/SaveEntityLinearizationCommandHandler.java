package edu.stanford.protege.webprotege.liniarizationservice.handlers;

import edu.stanford.protege.webprotege.liniarizationservice.services.*;
import edu.stanford.protege.webprotege.ipc.*;
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

        linearizationHistoryService.addRevision(request.entityLinearization(), request.projectId(), executionContext.userId());

        return Mono.just(SaveEntityLinearizationResponse.create());
    }
}
