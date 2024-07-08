package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;

public class SetGrouping extends LinearizationEvent {

    public final ThreeStateBoolean value;
    public SetGrouping(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationEvent applyEvent() {
        return this;
    }

    @Override
    public String getType() {
        return SetGrouping.class.getName();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }
}
