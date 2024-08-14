package edu.stanford.protege.webprotege.linearizationservice.events;


import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationChangeVisitor;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;

import static org.apache.commons.lang3.ObjectUtils.notEqual;


public class SetLinearizationParent extends LinearizationSpecificationEvent {

    public static final String DISCRIMINATOR = "SetLinearizationParent";
    private final String value;

    @JsonCreator
    public SetLinearizationParent(@JsonProperty("value") String value, @JsonProperty("linearizationView") String linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationSpecification specification)) {
            throw new RuntimeException("Error! Trying to parse event" + LinearizationSpecification.class.getName());
        }

        if (notEqual(specification.getLinearizationParent(), value)) {
            return new LinearizationSpecification(specification.getIsAuxiliaryAxisChild(),
                    specification.getIsGrouping(),
                    specification.getIsIncludedInLinearization(),
                    IRI.create(value),
                    specification.getLinearizationView(),
                    specification.getCodingNote());
        }

        return specification;
    }

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value;
    }

    @Override
    public <R> R accept(@NotNull LinearizationChangeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
