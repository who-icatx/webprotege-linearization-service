package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

public class SetGrouping extends LinearizationEvent {

    public final ThreeStateBoolean value;
    public SetGrouping(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return input;
    }

    @Override
    public String getType() {
        return SetGrouping.class.getName();
    }
}
