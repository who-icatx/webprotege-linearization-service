package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import com.fasterxml.jackson.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;


@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "EntityLinearizationHistories")
public class EntityLinearizationHistory {

    public static final String WHOFIC_ENTITY_IRI = "whoficEntityIri";
    public static final String PROJECT_ID = "projectId";
    public static final String LINEARIZATION_REVISIONS = "linearizationRevisions";

    @Field("whoficEntityIri")
    private final String whoficEntityIri;

    @Field("projectId")
    private final String projectId;

    @Field("linearizationRevisions")
    private final Set<LinearizationRevision> linearizationRevisions;

    @JsonCreator
    public EntityLinearizationHistory(@JsonProperty("whoficEntityIri") String whoficEntityIri,
                                      @JsonProperty("projectId") String projectId,
                                      @JsonProperty("linearizationRevisions") Set<LinearizationRevision> linearizationRevisions) {
        this.whoficEntityIri = whoficEntityIri;
        this.projectId = projectId;
        this.linearizationRevisions = linearizationRevisions;
    }


    public String getWhoficEntityIri() {
        return whoficEntityIri;
    }

    public String getProjectId() {
        return projectId;
    }

    public Set<LinearizationRevision> getLinearizationRevisions() {
        return linearizationRevisions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(whoficEntityIri, projectId, linearizationRevisions);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof EntityLinearizationHistory other)) {
            return false;
        }
        return this.whoficEntityIri.equals(other.whoficEntityIri) && this.projectId.equals(other.projectId) && this.linearizationRevisions.equals(other.linearizationRevisions);
    }


    @Override
    public String toString() {
        return toStringHelper("EntityLinearizationHistory").addValue(whoficEntityIri).addValue(projectId).addValue(linearizationRevisions).toString();
    }
}
