package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.ProjectLinearizationChangedEvent;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;

@WebProtegeHandler
public class SaveEntityLinearizationCommandHandler implements CommandHandler<SaveEntityLinearizationRequest, SaveEntityLinearizationResponse> {


    private final LinearizationHistoryService linearizationHistoryService;

    private final LinearizationChangeEmitterService linChangeEmitter;


    public SaveEntityLinearizationCommandHandler(LinearizationHistoryService linearizationHistoryService,
                                                 LinearizationChangeEmitterService linChangeEmitter) {
        this.linearizationHistoryService = linearizationHistoryService;
        this.linChangeEmitter = linChangeEmitter;
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

        linChangeEmitter.emitLinearizationChangeEvent(request.projectId(), List.of(ProjectLinearizationChangedEvent.create(EventId.generate(), request.projectId())));

        return Mono.just(SaveEntityLinearizationResponse.create());
    }
}
