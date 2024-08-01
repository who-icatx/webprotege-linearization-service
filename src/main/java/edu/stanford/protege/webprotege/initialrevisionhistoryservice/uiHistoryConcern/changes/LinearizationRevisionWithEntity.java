package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;
import org.semanticweb.owlapi.model.IRI;

class LinearizationRevisionWithEntity {
    private final LinearizationRevision revision;
    private final IRI whoficEntityIri;

    public LinearizationRevisionWithEntity(LinearizationRevision revision, IRI whoficEntityIri) {
        this.revision = revision;
        this.whoficEntityIri = whoficEntityIri;
    }

    public LinearizationRevision getRevision() {
        return revision;
    }

    public IRI getWhoficEntityIri() {
        return whoficEntityIri;
    }
}

