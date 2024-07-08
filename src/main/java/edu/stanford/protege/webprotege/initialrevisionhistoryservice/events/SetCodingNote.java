package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;

public class SetCodingNote  extends LinearizationEvent {

    public final String value;

    public SetCodingNote(String value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationEvent applyEvent() {
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
}
