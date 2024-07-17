package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.semanticweb.owlapi.model.IRI;

public class SetIncludedInLinearization extends LinearizationSpecificationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetIncludedInLinearization";

    public final ThreeStateBoolean value;

    @JsonCreator
    public SetIncludedInLinearization(@JsonProperty("value") ThreeStateBoolean value, @JsonProperty("linearizationView") IRI linearizationView) {
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
