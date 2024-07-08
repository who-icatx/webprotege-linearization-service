package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.semanticweb.owlapi.model.IRI;


@BsonDiscriminator(key = "type")
public abstract class LinearizationSpecificationEvent implements LinearizationEvent {

    private final IRI linearizationView;


    protected LinearizationSpecificationEvent(IRI linearizationView) {
        this.linearizationView = linearizationView;
    }


    IRI getLinearizationView() {
        return linearizationView;
    }

}
