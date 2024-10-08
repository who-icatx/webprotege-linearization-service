package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationResponse;

import java.util.Set;

public record LinearizationRevision(long timestamp, String userId, Set<LinearizationEvent> linearizationEvents) {

}
