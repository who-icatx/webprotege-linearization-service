package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationSpecification;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;

public class SetCodingNote extends LinearizationSpecificationEvent {

    private final String value;

    public final static String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetCodingNote";

    @JsonCreator
    public SetCodingNote(@JsonProperty("value") String value, @JsonProperty("linearizationView") IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationSpecification specification)) {
            throw new RuntimeException("Error! Trying to parse event" + LinearizationSpecification.class.getName());
        }

        if (specification.getCodingNote() == null ||
                isNotEquals(specification.getCodingNote(), value)) {
            return new LinearizationSpecification(specification.getIsAuxiliaryAxisChild(),
                    specification.getIsGrouping(),
                    specification.getIsIncludedInLinearization(),
                    specification.getLinearizationParent(),
                    specification.getLinearizationView(),
                    value);
        }

        return specification;
    }

    @Override
    public String getType() {
        return SetCodingNote.class.getName();
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value;
    }
}
