package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;

public record LinearizationRevision(long timestamp, UserId userId, Set<LinearizationEvent> linearizationEvents) implements Comparable<LinearizationRevision>{

    public static final String TIMESTAMP = "timestamp";
    public static final String USER_ID = "userId";
    public static final String LINEARIZATION_EVENTS = "linearizationEvents";

    public static LinearizationRevision create(UserId userId, Set<LinearizationEvent> linearizationEvents) {
        return new LinearizationRevision(Instant.now().toEpochMilli(), userId, linearizationEvents);
    }

    @Override
    public int compareTo(@NotNull LinearizationRevision other) {
        return Long.compare(this.timestamp, other.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, userId, linearizationEvents);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LinearizationRevision other)) {
            return false;
        }
        return this.timestamp == other.timestamp;
    }


    @Override
    public String toString() {
        return toStringHelper("LinearizationRevision")
                .addValue(timestamp)
                .addValue(userId)
                .addValue(linearizationEvents)
                .toString();
    }
}
