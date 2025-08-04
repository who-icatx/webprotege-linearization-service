package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.events.ParentsChangedEvent;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.PackagedProjectChangeEvent;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.Collections;

@WebProtegeHandler
public class SaveEntityLinearizationCommandHandler implements CommandHandler<SaveEntityLinearizationRequest, SaveEntityLinearizationResponse> {


    private final LinearizationHistoryService linearizationHistoryService;
    private final EventDispatcher eventDispatcher;


    public SaveEntityLinearizationCommandHandler(LinearizationHistoryService linearizationHistoryService, EventDispatcher eventDispatcher) {
        this.linearizationHistoryService = linearizationHistoryService;
        this.eventDispatcher = eventDispatcher;
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

        EventId eventId = EventId.generate();
        eventDispatcher.dispatchEvent(new PackagedProjectChangeEvent(request.projectId(), eventId, Collections.singletonList(new ParentsChangedEvent(request.projectId(), eventId, request.entityLinearization().entityIRI()))));

        return Mono.just(SaveEntityLinearizationResponse.create());
    }
}
