package edu.stanford.protege.webprotege.linearizationservice.model;

import com.fasterxml.jackson.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.*;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;
import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.LINEARIZATION_HISTORY_COLLECTION;
import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.WHOFIC_ENTITY_IRI;
import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.PROJECT_ID;


@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = LINEARIZATION_HISTORY_COLLECTION)
@CompoundIndexes({
        @CompoundIndex(name = "entity_iri_project_idx", def = "{'" + WHOFIC_ENTITY_IRI + "': 1, '" + PROJECT_ID + "': 1}")
})
public class EntityLinearizationHistory {

    public static final String WHOFIC_ENTITY_IRI = "whoficEntityIri";
    public static final String PROJECT_ID = "projectId";
    public static final String LINEARIZATION_REVISIONS = "linearizationRevisions";
    public static final String LINEARIZATION_HISTORY_COLLECTION = "EntityLinearizationHistory";

    @Field("whoficEntityIri")
    @Indexed(name = "whoficEntityIri_idx")
    private final String whoficEntityIri;

    @Field("projectId")
    @Indexed(name = "entityProjectId_idx")
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

    public static EntityLinearizationHistory create(String whoficEntityIri,
                                                    String projectId,
                                                    Set<LinearizationRevision> linearizationRevisions) {
        return new EntityLinearizationHistory(whoficEntityIri, projectId, linearizationRevisions);
    }
}
