package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;

import java.util.List;

public class LinearizationEventsForView {

    private final String viewName;
    private final List<LinearizationEvent> linearizationEvents;

    private LinearizationEventsForView(String viewName, List<LinearizationEvent> linearizationEvents){
        this.viewName = viewName;
        this.linearizationEvents = linearizationEvents;
    }

    public static LinearizationEventsForView create(String viewName, List<LinearizationEvent> linearizationEvents){
        return new LinearizationEventsForView(viewName, linearizationEvents);
    }

    public String getViewName() {
        return viewName;
    }

    public List<LinearizationEvent> getLinearizationEvents() {
        return linearizationEvents;
    }
}
