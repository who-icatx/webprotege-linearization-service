package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import org.jetbrains.annotations.NotNull;

public class SetSuppressedSpecifiedResidual implements LinearizationEvent {

    private ThreeStateBoolean value;

    public SetSuppressedSpecifiedResidual(ThreeStateBoolean value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return SetSuppressedSpecifiedResidual.class.getName();
    }

    @Override
    public LinearizationEvent applyEvent(LinearizationEvent event) {

        if(event.getValue().equals(this.value.name())){
            return this;
        }

        this.value = ThreeStateBoolean.valueOf(event.getValue());
        return this;
    }

    @Override
    public void accept(@NotNull EventChangeVisitor visitor) {
        visitor.visit(this);
    }

    public String getValue(){
        return this.value.name();
    }
}
