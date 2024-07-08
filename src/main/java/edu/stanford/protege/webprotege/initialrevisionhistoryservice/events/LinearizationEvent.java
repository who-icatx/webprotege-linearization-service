package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.semanticweb.owlapi.model.IRI;


@BsonDiscriminator(key = "type")
public abstract class LinearizationEvent {

    private final IRI linearizationView;


    protected LinearizationEvent(IRI linearizationView) {
        this.linearizationView = linearizationView;
    }

    abstract LinearizationResponse applyEvent(LinearizationResponse input);


    abstract String getType();

    IRI getLinearizationView() {
        return linearizationView;
    }

}
