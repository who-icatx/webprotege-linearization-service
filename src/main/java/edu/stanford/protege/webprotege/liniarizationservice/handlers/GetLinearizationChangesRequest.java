package edu.stanford.protege.webprotege.liniarizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.OWLEntity;

import static edu.stanford.protege.webprotege.liniarizationservice.handlers.GetLinearizationChangesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetLinearizationChangesRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("entity") OWLEntity subject,
        @JsonProperty("pageRequest") PageRequest pageRequest
) implements Request<GetLinearizationChangesResponse> {

    public static final String CHANNEL = "webprotege.linearization.GetLinearizationChanges";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
