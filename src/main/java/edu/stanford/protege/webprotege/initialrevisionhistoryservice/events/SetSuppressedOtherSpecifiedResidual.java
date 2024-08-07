package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes.LinearizationChangeVisitor;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetSuppressedOtherSpecifiedResidual implements LinearizationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetSuppressedOtherSpecifiedResidual";
    private final ThreeStateBoolean value;

    @JsonCreator
    public SetSuppressedOtherSpecifiedResidual(@JsonProperty("value") ThreeStateBoolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetSuppressedOtherSpecifiedResidual.class.getName();
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationResiduals residual)) {
            throw new RuntimeException("Error! Trying to parse event that is not " + LinearizationResiduals.class.getName());
        }

        if (notEqual(residual.getSuppressOtherSpecifiedResiduals(), value)) {
            return new LinearizationResiduals(value,
                    residual.getSuppressUnspecifiedResiduals(),
                    residual.getOtherSpecifiedResidualTitle(),
                    residual.getUnspecifiedResidualTitle());
        }

        return residual;
    }

    public String getValue() {
        return this.value.name();
    }

    @Override
    public <R> R accept(@NotNull LinearizationChangeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
