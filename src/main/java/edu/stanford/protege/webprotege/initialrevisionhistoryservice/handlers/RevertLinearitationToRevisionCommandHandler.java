package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.RevertLinearitationToRevisionRequest.CHANNEL;

@WebProtegeHandler
public class RevertLinearitationToRevisionCommandHandler implements CommandHandler<RevertLinearitationToRevisionRequest, RevertLinearitationToRevisionResponse> {

    LinearizationHistoryService historyService;
    LinearizationEventsProcessorService eventsProcessorService;

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
        });

        return Mono.just(RevertLinearitationToRevisionResponse.create());
    }
}
