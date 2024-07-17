package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;

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
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return null;
    }
}
