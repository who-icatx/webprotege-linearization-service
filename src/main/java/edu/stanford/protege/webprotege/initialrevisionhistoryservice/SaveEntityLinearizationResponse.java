package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.SaveEntityLinearizationRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record SaveEntityLinearizationResponse() implements Response {


    public static SaveEntityLinearizationResponse create() {
        return new SaveEntityLinearizationResponse();
    }
}
