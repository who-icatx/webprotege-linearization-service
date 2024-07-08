package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@BsonDiscriminator(key = "type")
public interface LinearizationEvent {

    String getType();
    LinearizationResponse applyEvent(LinearizationResponse input);

}
