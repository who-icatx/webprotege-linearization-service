package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.SaveEntityLinearizationRequest.CHANNEL;

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
