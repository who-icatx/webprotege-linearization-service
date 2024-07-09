package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

public class SetLinearizationParent extends LinearizationSpecificationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetLinearizationParent";
    public final IRI value;

    @JsonCreator
    public SetLinearizationParent(@JsonProperty("linearizationParent") IRI linearizationParent,@JsonProperty("linearizationView") IRI linearizationView) {
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
