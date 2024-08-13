package edu.stanford.protege.webprotege.liniarizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Request;

import static edu.stanford.protege.webprotege.liniarizationservice.handlers.LinearizationDefinitionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record LinearizationDefinitionRequest() implements Request<LinearizationDefinitionResponse> {

    public static final String CHANNEL = "webprotege.linearization.GetLinearizationDefinitions";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
