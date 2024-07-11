package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record LinearizationRevision(long timestamp, String userId, Set<LinearizationEvent> linearizationEvents) implements Comparable<LinearizationRevision>{

    @Override
    public int compareTo(@NotNull LinearizationRevision other) {
        return Long.compare(this.timestamp, other.timestamp);
    }
}
