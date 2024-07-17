package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@WebProtegeHandler
public class GetLinearizationDefinitionsCommandHandler implements CommandHandler<LinearizationDefinitionRequest, LinearizationDefinitionResponse> {

    private final String CHANNEL_NAME = "webprotege.linearization.GetLinearizationDefinitions";

    private final LinearizationDefinitionRepository repository;

    public GetLinearizationDefinitionsCommandHandler(LinearizationDefinitionRepository repository) {
        this.repository = repository;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL_NAME;
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
