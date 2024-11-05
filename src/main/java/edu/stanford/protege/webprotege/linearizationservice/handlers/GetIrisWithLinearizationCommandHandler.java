package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.GetIrisWithLinearizationRequest.CHANNEL;

@WebProtegeHandler
public class GetIrisWithLinearizationCommandHandler implements CommandHandler<GetIrisWithLinearizationRequest, GetIrisWithLinearizationResponse> {

    private final LinearizationHistoryService linearizationHistoryService;

    @Value("${webprotege.linearization.batch-size:100}")
    private int batchSize;


    public GetIrisWithLinearizationCommandHandler(LinearizationHistoryService linearizationHistoryService) {
        this.linearizationHistoryService = linearizationHistoryService;
    }

    @NotNull
    @Override
    public String getChannelName() {
        return CHANNEL;
    }

    @Override
    public Class<GetIrisWithLinearizationRequest> getRequestClass() {
        return GetIrisWithLinearizationRequest.class;
    }

    @Override
    public Mono<GetIrisWithLinearizationResponse> handleRequest(GetIrisWithLinearizationRequest request, ExecutionContext executionContext) {

        var irisWithLinearization = linearizationHistoryService.getIrisWithHistory(request.iris(), request.projectId(), batchSize);


        return Mono.just(GetIrisWithLinearizationResponse.create(irisWithLinearization));
    }
}
