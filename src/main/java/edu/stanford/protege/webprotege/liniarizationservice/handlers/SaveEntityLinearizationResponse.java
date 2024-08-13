package edu.stanford.protege.webprotege.liniarizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotege.liniarizationservice.handlers.SaveEntityLinearizationRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record SaveEntityLinearizationResponse() implements Response {


    public static SaveEntityLinearizationResponse create() {
        return new SaveEntityLinearizationResponse();
    }
}
