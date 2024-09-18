package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.GetLinearizationChangesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetLinearizationChangesResponse(
        @JsonProperty("projectChanges") Page<ProjectChange> changes
) implements Response {

    @JsonProperty("projectChanges")
    public Page<ProjectChange> changes() {
        return this.changes;
    }

    public static GetLinearizationChangesResponse create(Page<ProjectChange> changes){
        return new GetLinearizationChangesResponse(changes);
    }
}
