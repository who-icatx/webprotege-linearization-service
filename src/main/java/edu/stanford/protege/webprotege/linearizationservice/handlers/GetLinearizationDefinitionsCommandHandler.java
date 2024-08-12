package edu.stanford.protege.webprotege.linearizationservice.handlers;


import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.LinearizationDefinitionRequest.CHANNEL;

@WebProtegeHandler
public class GetLinearizationDefinitionsCommandHandler implements CommandHandler<LinearizationDefinitionRequest, LinearizationDefinitionResponse> {


    private final LinearizationDefinitionRepository repository;

    public GetLinearizationDefinitionsCommandHandler(LinearizationDefinitionRepository repository) {
        this.repository = repository;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<LinearizationDefinitionRequest> getRequestClass() {
        return LinearizationDefinitionRequest.class;
    }

    @Override
    public Mono<LinearizationDefinitionResponse> handleRequest(LinearizationDefinitionRequest request, ExecutionContext executionContext) {
        var response = new LinearizationDefinitionResponse(repository.getLinearizationDefinitions());

        return Mono.just(response);
    }
}