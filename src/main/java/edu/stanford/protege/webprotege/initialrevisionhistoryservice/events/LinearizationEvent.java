package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;


@BsonDiscriminator(key = "type")
public interface LinearizationEvent {

    String getType();
    LinearizationResponse applyEvent(LinearizationResponse input);

    void accept(@Nonnull EventChangeVisitor visitor);

}
