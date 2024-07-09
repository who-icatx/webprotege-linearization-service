package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.semanticweb.owlapi.model.IRI;

public class SetCodingNote  extends LinearizationSpecificationEvent {

    public final String value;

    public final static String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetCodingNote";

    @JsonCreator
    public SetCodingNote(@JsonProperty("value") String value,@JsonProperty("linearizationView") IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return input;
    }

    @Override
    public String getType() {
        return SetCodingNote.class.getName();
    }
}
