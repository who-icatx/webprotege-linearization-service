package edu.stanford.protege.webprotege.linearizationservice.model;

import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.index.*;

import java.util.*;

import static com.google.common.base.MoreObjects.toStringHelper;

public record LinearizationRevision(@Indexed(name = "timestamp", direction = IndexDirection.DESCENDING) long timestamp,
                                    UserId userId,
                                    Set<LinearizationEvent> linearizationEvents,
                                    String changeRequestId,
                                    CommitStatus commitStatus) implements Comparable<LinearizationRevision> {

    public static final String TIMESTAMP = "timestamp";
    public static final String USER_ID = "userId";
    public static final String LINEARIZATION_EVENTS = "linearizationEvents";

    public static LinearizationRevision create(UserId userId,
                                               Set<LinearizationEvent> linearizationEvents,
                                               ChangeRequestId changeRequestId) {
        CommitStatus commitStatus = changeRequestId != null && changeRequestId.id() != null ? CommitStatus.UNCOMMITTED : CommitStatus.COMMITTED;
        return new LinearizationRevision(new Date().getTime(), userId, linearizationEvents, changeRequestId != null ? changeRequestId.id() : null, commitStatus);
    }

    public static LinearizationRevision create(UserId userId,
                                               Set<LinearizationEvent> linearizationEvents) {
        return new LinearizationRevision(new Date().getTime(), userId, linearizationEvents, null, CommitStatus.COMMITTED);
    }

    public static LinearizationRevision createCommittedClone(LinearizationRevision linearizationRevision) {
        return new LinearizationRevision(linearizationRevision.timestamp,
                linearizationRevision.userId,
                linearizationRevision.linearizationEvents,
                linearizationRevision.changeRequestId,
                CommitStatus.COMMITTED);
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
