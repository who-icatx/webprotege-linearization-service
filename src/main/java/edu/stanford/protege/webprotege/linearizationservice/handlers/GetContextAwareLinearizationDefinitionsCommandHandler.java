package edu.stanford.protege.webprotege.linearizationservice.handlers;


import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinition;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinitionAccessibility;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationDefinitionService;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@WebProtegeHandler
public class GetContextAwareLinearizationDefinitionsCommandHandler implements CommandHandler<ContextAwareLinearizationDefinitionRequest, ContextAwareLinearizationDefinitionResponse> {
    private final LinearizationDefinitionRepository repository;

    private final LinearizationDefinitionService linearizationDefinitionService;

    public GetContextAwareLinearizationDefinitionsCommandHandler(LinearizationDefinitionRepository repository,
                                                                 LinearizationDefinitionService linearizationDefinitionService) {
        this.repository = repository;
        this.linearizationDefinitionService = linearizationDefinitionService;
    }


    @NotNull
    @Override
    public String getChannelName() {
        return ContextAwareLinearizationDefinitionRequest.CHANNEL;
    }

    @Override
    public Class<ContextAwareLinearizationDefinitionRequest> getRequestClass() {
        return ContextAwareLinearizationDefinitionRequest.class;
    }

    @Override
    public Mono<ContextAwareLinearizationDefinitionResponse> handleRequest(ContextAwareLinearizationDefinitionRequest request, ExecutionContext executionContext) {

        List<LinearizationDefinition> baseDefList = repository.getLinearizationDefinitions();

        List<LinearizationDefinition> response = new ArrayList<>();
        try {
            LinearizationDefinitionService.AllowedLinearizationDefinitions allowedDefs = linearizationDefinitionService.getUserAccessibleLinearizations(request.projectId(), request.entityIRI(), executionContext);

            for(LinearizationDefinition definition : baseDefList) {
                if(allowedDefs.editableLinearizations().contains(definition.getLinearizationUri()) ||
                        allowedDefs.readableLinearizations().contains(definition.getLinearizationUri())) {
                    if(allowedDefs.editableLinearizations().contains(definition.getLinearizationUri())) {
                        definition.setAccessibility(LinearizationDefinitionAccessibility.EDITABLE);
                    }
                    response.add(definition);
                }
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        return Mono.just(new ContextAwareLinearizationDefinitionResponse(response));
    }
}
