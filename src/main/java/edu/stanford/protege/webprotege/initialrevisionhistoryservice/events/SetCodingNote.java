package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import org.semanticweb.owlapi.model.IRI;

public class SetCodingNote  extends LinearizationSpecificationEvent {

    private String value;

    public SetCodingNote(String value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
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
    public String getType() {
        return SetCodingNote.class.getName();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }

    @JsonProperty("value")
    public String getValue(){
        return this.value;
    }
}
