package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;


import org.bson.codecs.pojo.annotations.BsonDiscriminator;



@BsonDiscriminator(key = "type")
public interface LinearizationEvent {

    String getType();
    EventProcesableParameter applyEvent(EventProcesableParameter input);

    void accept(EventChangeVisitor visitor);

    String getValue();
}
