package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRevision;

class LinearizationRevisionWithEntity {
    private final LinearizationRevision revision;
    private final String whoficEntityName;

    public LinearizationRevisionWithEntity(LinearizationRevision revision, String whoficEntityName) {
        this.revision = revision;
        this.whoficEntityName = whoficEntityName;
    }

    public LinearizationRevision getRevision() {
        return revision;
    }

    public String getWhoficEntityName() {
        return whoficEntityName;
    }
}

