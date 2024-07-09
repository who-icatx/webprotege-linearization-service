package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

public class SetAuxiliaryAxisChild extends LinearizationSpecificationEvent {

    public final ThreeStateBoolean value;

    public final static String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetAuxiliaryAxisChild";

    @JsonCreator
    public SetAuxiliaryAxisChild(@JsonProperty("value") ThreeStateBoolean value, @JsonProperty("linearizationView") IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return null;
    }

    @Override
    public String getType() {
        return SetAuxiliaryAxisChild.class.getName();
    }

    public static String getName(){
        return SetAuxiliaryAxisChild.class.getName();
    }
}
