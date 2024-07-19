package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetSuppressedSpecifiedResidual implements LinearizationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetSuppressedSpecifiedResidual";
    private final ThreeStateBoolean value;

    @JsonCreator
    public SetSuppressedSpecifiedResidual(@JsonProperty("value") ThreeStateBoolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetSuppressedSpecifiedResidual.class.getName();
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationResiduals residual)) {
            throw new RuntimeException("Error! Trying to parse event that is not " + LinearizationResiduals.class.getName());
        }

        if (notEqual(residual.getSuppressSpecifiedResidual(), value)) {
            return new LinearizationResiduals(value, residual.getUnspecifiedResidualTitle());
        }

        return residual;
    }

    public String getValue() {
        return this.value.name();
    }
}
