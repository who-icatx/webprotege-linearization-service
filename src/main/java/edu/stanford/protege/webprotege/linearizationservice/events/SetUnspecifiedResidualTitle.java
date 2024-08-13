package edu.stanford.protege.webprotege.liniarizationservice.events;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.liniarizationservice.model.LinearizationResiduals;
import edu.stanford.protege.webprotege.liniarizationservice.uiHistoryConcern.changes.LinearizationChangeVisitor;
import org.jetbrains.annotations.NotNull;

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
            return new LinearizationResiduals(residual.getSuppressOtherSpecifiedResiduals(),
                    residual.getSuppressUnspecifiedResiduals(),
                    residual.getOtherSpecifiedResidualTitle(),
                    value);
        }

        return residual;
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value != null ? this.value : "";
    }

    @Override
    public <R> R accept(@NotNull LinearizationChangeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
