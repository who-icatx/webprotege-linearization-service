package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationResiduals;

import static org.apache.commons.lang3.ObjectUtils.notEqual;

public class SetOtherSpecifiedResidualTitle implements LinearizationEvent {

    public static final String CLASS_TYPE = "edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetOtherSpecifiedResidualTitle";
    private final String value;

    @JsonCreator
    public SetOtherSpecifiedResidualTitle(@JsonProperty("value") String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetOtherSpecifiedResidualTitle.class.getName();
    }

    @Override
    public EventProcesableParameter applyEvent(EventProcesableParameter event) {
        if (!(event instanceof LinearizationResiduals residual)) {
            throw new RuntimeException("Error! Trying to parse event that is not " + LinearizationResiduals.class.getName());
        }

        if (notEqual(residual.getUnspecifiedResidualTitle(), value)) {
            return new LinearizationResiduals(residual.getSuppressOtherSpecifiedResiduals(),
                    residual.getSuppressUnspecifiedResiduals(),
                    value,
                    residual.getUnspecifiedResidualTitle());
        }

        return residual;
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value;
    }
}
