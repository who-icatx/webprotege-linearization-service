package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

public class SetAuxiliaryAxisChild extends LinearizationSpecificationEvent {

    private ThreeStateBoolean value;

    public SetAuxiliaryAxisChild(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationEvent applyEvent(LinearizationEvent event) {
        if(event.getValue().equals(this.value.name())){
            return this;
        }

        this.value = ThreeStateBoolean.valueOf(event.getValue());
        return this;
    }

    @Override
    public String getType() {
        return SetAuxiliaryAxisChild.class.getName();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }

    public String getValue(){
        return this.value.name();
    }
}
