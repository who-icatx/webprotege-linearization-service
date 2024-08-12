package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.GetEntityLinearizationsRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityLinearizationsRequest(@JsonProperty("entityIRI") String entityIRI,
                                             @JsonProperty("projectId") ProjectId projectId) implements Request<GetEntityLinearizationsResponse> {

    public static final String CHANNEL = "webprotege.linearization.GetEntityLinearizations";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}
