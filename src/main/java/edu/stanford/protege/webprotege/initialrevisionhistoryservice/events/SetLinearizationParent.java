package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationSpecification;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;

public class SetLinearizationParent extends LinearizationSpecificationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetLinearizationParent";
    private final IRI value;

    @JsonCreator
    public SetLinearizationParent(@JsonProperty("linearizationParent") IRI linearizationParent,@JsonProperty("linearizationView") IRI linearizationView) {
        super(linearizationView);
        this.value = linearizationParent;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if(!(event instanceof LinearizationSpecification specification)){
            throw new RuntimeException("Error! Trying to parse event"+LinearizationSpecification.class.getName());
        }

        if (isNotEquals(specification.getIsAuxiliaryAxisChild(), value)){
            return new LinearizationSpecification(specification.getIsAuxiliaryAxisChild(),
                    specification.getIsGrouping(),
                    specification.getIsIncludedInLinearization(),
                    value,
                    specification.getLinearizationView(),
                    specification.getCodingNote());
        }

        return specification;
    }

    @Override
    public String getType() {
        return SetLinearizationParent.class.getName();
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value.toString();
    }

    @Override
    public void accept(@Nonnull EventVisitor visitor) {
        visitor.visit(this);
    }
}
