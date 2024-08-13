package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;

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

