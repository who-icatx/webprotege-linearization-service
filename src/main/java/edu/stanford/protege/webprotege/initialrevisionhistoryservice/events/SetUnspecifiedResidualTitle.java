package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

public class SetUnspecifiedResidualTitle  implements LinearizationEvent {

    private final String value;

    public SetUnspecifiedResidualTitle(String value) {
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
