package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.ThreeStateBoolean;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

public class SetIncludedInLinearization extends LinearizationSpecificationEvent {

    private ThreeStateBoolean value;

    public SetIncludedInLinearization(ThreeStateBoolean value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationEvent applyEvent(LinearizationEvent event) {

        if(!event.getValue().equals(this.value.name())){
            this.value = ThreeStateBoolean.valueOf(event.getValue());
        }

        return this;
    }

    @Override
    public String getType() {
        return SetIncludedInLinearization.class.getName();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }

    @JsonProperty("value")
    public String getValue(){
        return this.value.name();
    }
}
