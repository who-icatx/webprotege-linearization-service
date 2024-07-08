package edu.stanford.protege.webprotege.initialrevisionhistoryservice.events;

public interface LinearizationEvent {

    String getType();
    LinearizationResponse applyEvent(LinearizationResponse input);

}
