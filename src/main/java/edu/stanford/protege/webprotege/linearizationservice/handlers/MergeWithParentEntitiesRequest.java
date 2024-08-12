package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

@JsonTypeName(MergeWithParentEntitiesRequest.CHANNEL)
public record MergeWithParentEntitiesRequest(
        @JsonProperty("currentEntityIri") IRI currentEntityIri,
        @JsonProperty("parentEntityIris") Set<IRI> parentEntityIris,
        @JsonProperty("projectId") ProjectId projectId
) implements Request<MergeWithParentEntitiesResponse> {

    public final static String CHANNEL = "webprotege.linearization.MergeWithParentEntities";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static MergeWithParentEntitiesRequest create(IRI currentEntityIri, Set<IRI> parentEntityIris, ProjectId projectId) {
        return new MergeWithParentEntitiesRequest(currentEntityIri, parentEntityIris, projectId);
    }
}
