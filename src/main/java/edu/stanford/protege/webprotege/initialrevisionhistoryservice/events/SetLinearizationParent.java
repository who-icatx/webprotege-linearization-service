package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

public class SetLinearizationParent extends LinearizationSpecificationEvent {

    public final IRI value;
    public SetLinearizationParent(IRI linearizationParent, IRI linearizationView) {
        super(linearizationView);
        this.value = linearizationParent;
    }

    @Override
    public LinearizationSpecificationEvent applyEvent() {
        return null;
    }

    @Override
    public String getType() {
        return SetLinearizationParent.class.getName();
    }

    @JsonProperty("value")
    public String getValue(){
        return this.value.toString();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }
}
