package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityLinearizationHistory {

    private final String whoficEntityIri;

    private final ProjectId projectId;

    private final Set<LinearizationRevision> linearizationRevisions;

    @JsonCreator
    public EntityLinearizationHistory(@JsonProperty("whoficEntityIri")  String whoficEntityIri,
                                      @JsonProperty("projectId") ProjectId projectId,
                                      @JsonProperty("linearizationRevisions") Set<LinearizationRevision> linearizationRevisions) {
        this.whoficEntityIri = whoficEntityIri;
        this.projectId = projectId;
        this.linearizationRevisions = linearizationRevisions;
    }

    public static final String WHOFIC_ENTITY_IRI_KEY = "whoficEntityIri";

    public static final String PROJECT_ID = "projectId";


    public String getWhoficEntityIri() {
        return whoficEntityIri;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public Set<LinearizationRevision> getLinearizationRevisions() {
        return linearizationRevisions;
    }
}
