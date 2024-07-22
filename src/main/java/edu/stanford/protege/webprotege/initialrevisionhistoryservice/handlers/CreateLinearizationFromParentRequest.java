package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateLinearizationFromParentRequest(
        @JsonProperty("newEntityIri") IRI newEntityIri,
        @JsonProperty("parentEntityIri") IRI parentEntityIri
) implements Request<CreateLinearizationFromParentResponse> {

    public final static String CHANNEL = "webprotege.linearization.CreateFromParentEntity";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
