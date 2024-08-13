package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateLinearizationFromParentResponse() implements Response {
    public static CreateLinearizationFromParentResponse create() {
        return new CreateLinearizationFromParentResponse();
    }
}
