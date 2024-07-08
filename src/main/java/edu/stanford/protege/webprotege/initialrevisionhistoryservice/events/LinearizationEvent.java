package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

import edu.stanford.protege.webprotege.change.OntologyChangeVisitor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;


@BsonDiscriminator(key = "type")
public abstract class LinearizationEvent {

    private final IRI linearizationView;


    protected LinearizationEvent(IRI linearizationView) {
        this.linearizationView = linearizationView;
    }

    public abstract LinearizationEvent applyEvent();


    abstract String getType();

    IRI getLinearizationView() {
        return linearizationView;
    }

    public abstract void accept(@Nonnull EventChangeVisitor visitor);

}
