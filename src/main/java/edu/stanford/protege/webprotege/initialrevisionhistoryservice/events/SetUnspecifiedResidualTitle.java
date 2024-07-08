package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class SetUnspecifiedResidualTitle  implements LinearizationEvent {

    private String value;

    public SetUnspecifiedResidualTitle(String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetUnspecifiedResidualTitle.class.getName();
    }

    @Override
    public LinearizationEvent applyEvent(LinearizationEvent event) {

        if(event.getValue().equals(this.value)){
            return this;
        }

        this.value = event.getValue();
        return this;
    }

    @Override
    public void accept(@NotNull EventChangeVisitor visitor) {
        visitor.visit(this);
    }

    @JsonProperty("value")
    public String getValue(){
        return this.value;
    }
}
