package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.ProjectLinearizationChangedEvent;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.RevertLinearitationToRevisionRequest.CHANNEL;

@WebProtegeHandler
public class RevertLinearitationToRevisionCommandHandler implements CommandHandler<RevertLinearitationToRevisionRequest, RevertLinearitationToRevisionResponse> {

    private final LinearizationHistoryService historyService;
    private final LinearizationEventsProcessorService eventsProcessorService;

    private final LinearizationChangeEmitterService linChangeEmitter;

    public RevertLinearitationToRevisionCommandHandler(LinearizationHistoryService historyService,
                                                       LinearizationEventsProcessorService eventsProcessorService,
                                                       LinearizationChangeEmitterService linChangeEmitter) {
        this.historyService = historyService;
        this.eventsProcessorService = eventsProcessorService;
        this.linChangeEmitter = linChangeEmitter;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<RevertLinearitationToRevisionRequest> getRequestClass() {
        return RevertLinearitationToRevisionRequest.class;
    }

    @Override
    public Mono<RevertLinearitationToRevisionResponse> handleRequest(RevertLinearitationToRevisionRequest request, ExecutionContext executionContext) {
        var currentHistory = historyService.getExistingHistoryOrderedByRevision(request.entityIri(), request.projectId());

        currentHistory.ifPresent(history -> {
            var revisionsUntilTimestamp = history.getLinearizationRevisions()
                    .stream().
                    filter(linearizationRevision -> linearizationRevision.timestamp() <= request.revisionTimestamp())
                    .collect(Collectors.toSet());

            var newRevisionUntilTimestamp = eventsProcessorService.processHistory(revisionsUntilTimestamp, history.getWhoficEntityIri());

            historyService.addRevision(newRevisionUntilTimestamp, request.projectId(), executionContext.userId());

            linChangeEmitter.emitLinearizationChangeEvent(request.projectId(), List.of(ProjectLinearizationChangedEvent.create(EventId.generate(), request.projectId())));
        });

        return Mono.just(RevertLinearitationToRevisionResponse.create());
    }
}
