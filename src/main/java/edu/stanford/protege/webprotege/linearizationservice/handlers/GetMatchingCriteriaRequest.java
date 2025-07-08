package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import edu.stanford.protege.webprotege.dispatch.ProjectAction;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;
import java.util.Map;


@JsonTypeName(GetMatchingCriteriaRequest.CHANNEL)
public record GetMatchingCriteriaRequest(@JsonProperty("criteriaMap") Map<String, List<CompositeRootCriteria>> criteriaMap,
                                         @JsonProperty("projectId") ProjectId projectId,
                                         @JsonProperty("entityIri") IRI entitiyIri) implements ProjectAction<GetMatchingCriteriaResponse> {

    public static final String CHANNEL = "webprotege.entities.GetMatchingCriteria";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
