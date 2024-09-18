package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRevision;
import org.springframework.data.mongodb.core.mapping.Field;

import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.*;

public class LinearizationRevisionWithEntity {
    @Field(LINEARIZATION_REVISIONS)
    private final LinearizationRevision revision;
    @Field(WHOFIC_ENTITY_IRI)
    private final String whoficEntityIri;

    public LinearizationRevisionWithEntity(LinearizationRevision revision,
                                           String whoficEntityIri) {
        this.revision = revision;
        this.whoficEntityIri = whoficEntityIri;
    }

    public LinearizationRevision getRevision() {
        return revision;
    }

    public String getWhoficEntityIri() {
        return whoficEntityIri;
    }
}

