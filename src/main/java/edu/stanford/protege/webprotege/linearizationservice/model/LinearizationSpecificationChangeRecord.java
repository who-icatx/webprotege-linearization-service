package edu.stanford.protege.webprotege.liniarizationservice.model;

import edu.stanford.protege.webprotege.liniarizationservice.events.LinearizationSpecificationEvent;

import java.util.List;

public class LinearizationSpecificationChangeRecord {
    private final String userId;
    private final Long timestamp;

    private final List<LinearizationSpecificationEvent> eventList;

    public LinearizationSpecificationChangeRecord(String userId, Long timestamp, List<LinearizationSpecificationEvent> eventList) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.eventList = eventList;
    }
}
