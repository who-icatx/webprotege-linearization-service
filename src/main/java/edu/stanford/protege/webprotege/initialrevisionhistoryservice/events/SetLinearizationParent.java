package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

public class SetLinearizationParent extends LinearizationSpecificationEvent {

    private IRI value;

    public SetLinearizationParent(IRI linearizationParent, IRI linearizationView) {
        super(linearizationView);
        this.value = linearizationParent;
    }

    @Override
    public LinearizationEvent applyEvent(LinearizationEvent event) {
        if (event.getValue().equals(this.value.toString())) {
            return this;
        }

        this.value = IRI.create(event.getValue());
        return this;
    }

    @Override
    public String getType() {
        return SetLinearizationParent.class.getName();
    }

    @JsonProperty("value")
    public String getValue() {
        return this.value.toString();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor) {
        visitor.visit(this);
    }
}
