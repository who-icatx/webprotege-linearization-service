package edu.stanford.protege.webprotege.linearizationservice.events;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationChangeVisitor;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetGrouping extends LinearizationSpecificationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetGrouping";

    private final ThreeStateBoolean value;

    @JsonCreator
    public SetGrouping(@JsonProperty("value") ThreeStateBoolean value, @JsonProperty("linearizationView") String linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationSpecification specification)) {
            throw new RuntimeException("Error! Trying to parse event" + LinearizationSpecification.class.getName());
        }

        if (notEqual(specification.getIsGrouping(), value)) {
            return new LinearizationSpecification(specification.getIsAuxiliaryAxisChild(),
                    value,
                    specification.getIsIncludedInLinearization(),
                    specification.getLinearizationParent(),
                    specification.getLinearizationView(),
                    specification.getCodingNote());
        }

        return specification;
    }

    @Override
    public String getType() {
        return SetGrouping.class.getName();
    }

    public String getValue() {
        return this.value.name();
    }

    @Override
    public <R> R accept(@NotNull LinearizationChangeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
