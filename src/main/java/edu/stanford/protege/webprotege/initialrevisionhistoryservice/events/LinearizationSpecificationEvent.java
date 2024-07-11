package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;


public abstract class LinearizationSpecificationEvent implements LinearizationEvent {

    private final IRI linearizationView;


    protected LinearizationSpecificationEvent(IRI linearizationView) {
        this.linearizationView = linearizationView;
    }


    @JsonProperty("linearizationView")
    public IRI getLinearizationView() {
        return linearizationView;
    }

}
