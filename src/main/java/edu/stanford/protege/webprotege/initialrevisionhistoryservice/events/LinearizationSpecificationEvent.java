package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import org.semanticweb.owlapi.model.IRI;


public abstract class LinearizationSpecificationEvent implements LinearizationEvent {

    private final IRI linearizationView;


    protected LinearizationSpecificationEvent(IRI linearizationView) {
        this.linearizationView = linearizationView;
    }


    IRI getLinearizationView() {
        return linearizationView;
    }

}
