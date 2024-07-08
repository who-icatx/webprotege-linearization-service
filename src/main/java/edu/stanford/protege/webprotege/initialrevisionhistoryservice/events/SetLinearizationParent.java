package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

public class SetLinearizationParent extends LinearizationEvent {

    public final IRI value;
    public SetLinearizationParent(IRI linearizationParent, IRI linearizationView) {
        super(linearizationView);
        this.value = linearizationParent;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
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
}
