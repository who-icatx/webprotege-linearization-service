package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@WebProtegeHandler
public class CreateLinearizationFromParentCommandHandler implements CommandHandler<CreateLinearizationFromParentRequest, CreateLinearizationFromParentResponse> {
    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<CreateLinearizationFromParentRequest> getRequestClass() {
        return null;
    }

    @Override
    public Mono<CreateLinearizationFromParentResponse> handleRequest(CreateLinearizationFromParentRequest request, ExecutionContext executionContext) {
        return null;
    }
}
