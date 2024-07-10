package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.jetbrains.annotations.NotNull;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;

public class SetUnspecifiedResidualTitle  implements LinearizationEvent {

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
        if(!(event instanceof LinearizationResiduals residual)){
            throw new RuntimeException("Error! Trying to parse event that is not "+LinearizationResiduals.class.getName());
        }

        if (isNotEquals(residual.getSuppressSpecifiedResidual(), value)){
            return new LinearizationResiduals(residual.getSuppressSpecifiedResidual(), value);
        }

        return residual;
    }

    @Override
    public void accept(@NotNull EventVisitor visitor) {
        visitor.visit(this);
    }

    @JsonProperty("value")
    public String getValue(){
        return this.value;
    }
}
