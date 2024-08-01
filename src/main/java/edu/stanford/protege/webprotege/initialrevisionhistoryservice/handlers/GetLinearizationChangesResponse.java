package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes.ProjectChange;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.GetLinearizationChangesRequest.CHANNEL;

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
