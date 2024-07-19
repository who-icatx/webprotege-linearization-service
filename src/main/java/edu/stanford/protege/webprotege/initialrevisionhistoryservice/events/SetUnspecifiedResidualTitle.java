package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationResiduals;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetUnspecifiedResidualTitle implements LinearizationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetUnspecifiedResidualTitle";
    private final String value;

    @JsonCreator
    public SetUnspecifiedResidualTitle(@JsonProperty("value") String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetUnspecifiedResidualTitle.class.getName();
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationResiduals residual)) {
            throw new RuntimeException("Error! Trying to parse event that is not " + LinearizationResiduals.class.getName());
        }

        if (notEqual(residual.getUnspecifiedResidualTitle(), value)) {
            return new LinearizationResiduals(residual.getSuppressSpecifiedResidual(), value);
        }

        return residual;
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value;
    }
}
