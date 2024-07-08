package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import org.semanticweb.owlapi.model.IRI;

public class SetCodingNote  extends LinearizationSpecificationEvent {

    public final String value;

    public SetCodingNote(String value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationSpecificationEvent applyEvent() {
        return input;
    }

    @Override
    public String getType() {
        return SetCodingNote.class.getName();
    }

    @Override
    public void accept(@Nonnull EventChangeVisitor visitor){
        visitor.visit(this);
    }
}
