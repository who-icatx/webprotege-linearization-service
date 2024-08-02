package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationResiduals;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetSuppressedUnspecifiedResiduals implements LinearizationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetSuppressedUnspecifiedResiduals";
    private final ThreeStateBoolean value;

    @JsonCreator
    public SetSuppressedUnspecifiedResiduals(@JsonProperty("value") ThreeStateBoolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetSuppressedUnspecifiedResiduals.class.getName();
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationResiduals residual)) {
            throw new RuntimeException("Error! Trying to parse event that is not " + LinearizationResiduals.class.getName());
        }

        if (notEqual(residual.getSuppressOtherSpecifiedResiduals(), value)) {
            return new LinearizationResiduals(residual.getSuppressOtherSpecifiedResiduals(),
                    value,
                    residual.getOtherSpecifiedResidualTitle(),
                    residual.getUnspecifiedResidualTitle());
        }

        return residual;
    }

    public String getValue() {
        return this.value.name();
    }

}