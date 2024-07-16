package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;

import java.time.Instant;
import java.util.Set;

public record LinearizationRevision(long timestamp, UserId userId, Set<LinearizationEvent> linearizationEvents) implements Comparable<LinearizationRevision>{

    public static LinearizationRevision create(UserId userId, Set<LinearizationEvent> linearizationEvents) {
        return new LinearizationRevision(Instant.now().toEpochMilli(), userId, linearizationEvents);
    }
}
