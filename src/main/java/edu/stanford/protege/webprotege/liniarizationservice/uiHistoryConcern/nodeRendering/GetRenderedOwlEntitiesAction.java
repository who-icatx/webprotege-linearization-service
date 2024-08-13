package edu.stanford.protege.webprotege.liniarizationservice.uiHistoryConcern.nodeRendering;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.dispatch.ProjectAction;

import java.util.Set;

import static edu.stanford.protege.webprotege.liniarizationservice.uiHistoryConcern.nodeRendering.GetRenderedOwlEntitiesAction.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetRenderedOwlEntitiesAction(
        @JsonProperty("entityIris") Set<String> entityIris,
        @JsonProperty("projectId") ProjectId projectId
) implements ProjectAction<GetRenderedOwlEntitiesResult> {

    public static final String CHANNEL = "webprotege.entities.RenderedOwlEntities";

    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static GetRenderedOwlEntitiesAction create(Set<String> entityIris, ProjectId projectId) {
        return new GetRenderedOwlEntitiesAction(entityIris, projectId);
    }
}
