package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

@JsonTypeName(GetParentsThatAreLinearizationPathParentsRequest.CHANNEL)
public record GetParentsThatAreLinearizationPathParentsRequest(
        @JsonProperty("currentEntityIri") IRI currentEntityIri,
        @JsonProperty("parentEntityIris") Set<IRI> parentEntityIris,
        @JsonProperty("projectId") ProjectId projectId
) implements Request<GetParentsThatAreLinearizationPathParentsResponse> {

    public final static String CHANNEL = "webprotege.linearization.GetParentsThatAreLinearizationPathParents";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static GetParentsThatAreLinearizationPathParentsRequest create(IRI currentEntityIri, Set<IRI> parentEntityIris, ProjectId projectId) {
        return new GetParentsThatAreLinearizationPathParentsRequest(currentEntityIri, parentEntityIris, projectId);
    }

}
