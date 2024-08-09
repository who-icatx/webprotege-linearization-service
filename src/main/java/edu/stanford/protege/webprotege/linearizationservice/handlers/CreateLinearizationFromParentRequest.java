package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateLinearizationFromParentRequest(
        @JsonProperty("newEntityIri") IRI newEntityIri,
        @JsonProperty("parentEntityIri") IRI parentEntityIri,
        @JsonProperty("projectId") ProjectId projectId
) implements Request<CreateLinearizationFromParentResponse> {

    public final static String CHANNEL = "webprotege.linearization.CreateFromParentEntity";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
