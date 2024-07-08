package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import org.semanticweb.owlapi.model.IRI;

public class SetCodingNote  extends LinearizationEvent {

    public final String value;

    public SetCodingNote(String value, IRI linearizationView) {
        super(linearizationView);
        this.value = value;
    }

    @Override
    public LinearizationResponse applyEvent(LinearizationResponse input) {
        return input;
    }

    @Override
    public String getType() {
        return SetCodingNote.class.getName();
    }
}
