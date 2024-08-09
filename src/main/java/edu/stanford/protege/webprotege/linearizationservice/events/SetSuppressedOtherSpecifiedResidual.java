package edu.stanford.protege.webprotege.linearizationservice.events;


import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetSuppressedOtherSpecifiedResidual implements LinearizationEvent {

    public static final String DISCRIMINATOR = "SetSuppressedOtherSpecifiedResidual";
    private final ThreeStateBoolean value;

    @JsonCreator
    public SetSuppressedOtherSpecifiedResidual(@JsonProperty("value") ThreeStateBoolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return DISCRIMINATOR;
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
}