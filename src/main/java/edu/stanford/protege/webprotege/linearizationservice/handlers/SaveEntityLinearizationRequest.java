package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.SaveEntityLinearizationRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record SaveEntityLinearizationRequest(
        @JsonProperty("projectId")
        ProjectId projectId,
        @JsonProperty("entityLinearization")
        WhoficEntityLinearizationSpecification entityLinearization
) implements Request<SaveEntityLinearizationResponse> {

    public static final String CHANNEL = "webprotege.linearization.SaveEntityLinearization";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

}
