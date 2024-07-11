package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.jetbrains.annotations.NotNull;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEquals;

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
        if(!(event instanceof LinearizationResiduals residual)){
            throw new RuntimeException("Error! Trying to parse event that is not "+LinearizationResiduals.class.getName());
        }

        if (isNotEquals(residual.getSuppressSpecifiedResidual(), value)){
            return new LinearizationResiduals(value, residual.getUnspecifiedResidualTitle());
        }

        return residual;
    }

    @Override
    public void accept(@NotNull EventVisitor visitor) {
        visitor.visit(this);
    }

    public String getValue(){
        return this.value.name();
    }
}
