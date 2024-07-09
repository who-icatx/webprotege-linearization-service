package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;

public class SetIncludedInLinearization extends LinearizationSpecificationEvent {

    private final ThreeStateBoolean value;

    public SetIncludedInLinearization(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if(!(event instanceof LinearizationSpecification specification)){
            throw new RuntimeException("Error! Trying to parse event"+LinearizationSpecification.class.getName());
        }

        if (isNotEquals(specification.getIsAuxiliaryAxisChild(), value)){
            return new LinearizationSpecification(specification.getIsAuxiliaryAxisChild(),
                    specification.getIsGrouping(),
                    value,
                    specification.getLinearizationParent(),
                    specification.getLinearizationView(),
                    specification.getCodingNote());
        }

        return specification;
    }

    @Override
    public String getType() {
        return SetIncludedInLinearization.class.getName();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }

    @JsonProperty("value")
    public String getValue(){
        return this.value.name();
    }
}
