package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;

import java.util.List;

public class LinearizationSpecificationChangeRecord {
    private final String userId;
    private final Long timestamp;

    private final List<LinearizationEvent> eventList;

    public LinearizationSpecificationChangeRecord(String userId, Long timestamp, List<LinearizationEvent> eventList) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.eventList = eventList;
    }
}
