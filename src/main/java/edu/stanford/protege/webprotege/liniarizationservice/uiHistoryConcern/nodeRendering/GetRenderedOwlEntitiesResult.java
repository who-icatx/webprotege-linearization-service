package edu.stanford.protege.webprotege.liniarizationservice.uiHistoryConcern.nodeRendering;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.dispatch.Result;
import edu.stanford.protege.webprotege.entity.EntityNode;

import java.util.List;

import static edu.stanford.protege.webprotege.liniarizationservice.uiHistoryConcern.nodeRendering.GetRenderedOwlEntitiesAction.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetRenderedOwlEntitiesResult(@JsonProperty List<EntityNode> renderedEntities) implements Result {

}
