package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;

public class SetSuppressedSpecifiedResidual implements LinearizationEvent {

    private final ThreeStateBoolean value;

    public SetSuppressedSpecifiedResidual(ThreeStateBoolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return null;
    }
}
