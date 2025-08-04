package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

@JsonTypeName(GetParentsThatAreLinearizationPathParentsRequest.CHANNEL)
public record GetParentsThatAreLinearizationPathParentsResponse(
        @JsonProperty("parentsThatAreLinearizationPathParents") Set<IRI> parentsThatAreLinearizationPathParents,

        @JsonProperty("existingLinearizationParents") Set<IRI> existingLinearizationParents
) implements Response {
}
