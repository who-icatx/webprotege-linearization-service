package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes.*;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.GetLinearizationChangesRequest.CHANNEL;

@WebProtegeHandler
public class GetLinearizationChangesCommandHandler implements CommandHandler<GetLinearizationChangesRequest, GetLinearizationChangesResponse> {

    private final ProjectChangesManager projectChangesManager;

    public GetLinearizationChangesCommandHandler(ProjectChangesManager projectChangesManager) {
        this.projectChangesManager = projectChangesManager;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<GetLinearizationChangesRequest> getRequestClass() {
        return GetLinearizationChangesRequest.class;
    }

    @Override
    public Mono<GetLinearizationChangesResponse> handleRequest(GetLinearizationChangesRequest request, ExecutionContext executionContext) {
        Page<ProjectChange> pageWithChanges = projectChangesManager.getProjectChanges(request.subject(), request.projectId(), request.pageRequest());
        return Mono.just(GetLinearizationChangesResponse.create(pageWithChanges));
    }
}
