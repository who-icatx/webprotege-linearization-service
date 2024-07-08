package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

public class SetIncludedInLinearization extends LinearizationEvent {

    public final ThreeStateBoolean value;

    public SetIncludedInLinearization(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return null;
    }

    @Override
    public String getType() {
        return SetIncludedInLinearization.class.getName();
    }
}
