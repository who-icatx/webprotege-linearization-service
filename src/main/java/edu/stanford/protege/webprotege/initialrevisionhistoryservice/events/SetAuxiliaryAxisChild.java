package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;

public class SetAuxiliaryAxisChild extends LinearizationSpecificationEvent {

    private final ThreeStateBoolean value;

    public SetAuxiliaryAxisChild(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {

        if(!(event instanceof LinearizationSpecification specification)){
          throw new RuntimeException("Error! Trying to parse event"+LinearizationSpecification.class.getName());
        }

        if (isNotEquals(specification.getIsAuxiliaryAxisChild(), value)){
            return new LinearizationSpecification(value,
                    specification.getIsGrouping(),
                    specification.getIsIncludedInLinearization(),
                    specification.getLinearizationParent(),
                    specification.getLinearizationView(),
                    specification.getCodingNote());
        }

        return specification;
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
