package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.RevertLinearitationToRevisionRequest.CHANNEL;

@WebProtegeHandler
public class RevertLinearitationToRevisionCommandHandler implements CommandHandler<RevertLinearitationToRevisionRequest, RevertLinearitationToRevisionResponse> {

    private final LinearizationHistoryService historyService;
    private final LinearizationEventsProcessorService eventsProcessorService;


    public RevertLinearitationToRevisionCommandHandler(LinearizationHistoryService historyService,
                                                       LinearizationEventsProcessorService eventsProcessorService) {
        this.historyService = historyService;
        this.eventsProcessorService = eventsProcessorService;
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

            var newRevisionUntilTimestamp = eventsProcessorService.processHistory(revisionsUntilTimestamp,
                    request.projectId(),
                    executionContext,
                    history.getWhoficEntityIri());

            historyService.addRevision(newRevisionUntilTimestamp,executionContext, request.projectId(), executionContext.userId());

        });

        return Mono.just(RevertLinearitationToRevisionResponse.create());
    }
}
