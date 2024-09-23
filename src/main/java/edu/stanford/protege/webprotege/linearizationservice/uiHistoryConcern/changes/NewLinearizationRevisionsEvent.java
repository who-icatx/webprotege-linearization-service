package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.NewLinearizationRevisionsEvent.CHANNEL;


@JsonTypeName(CHANNEL)
public record NewLinearizationRevisionsEvent(
        EventId eventId,
        ProjectId projectId,
        Set<ProjectChangeForEntity> changes
) implements ProjectEvent {
    public final static String CHANNEL = "webprotege.events.projects.linearizations.NewLinearizationRevisionsEvent";

    public static NewLinearizationRevisionsEvent create(EventId eventId,
                                                        ProjectId projectId,
                                                        Set<ProjectChangeForEntity> changes) {
        return new NewLinearizationRevisionsEvent(eventId, projectId, changes);
    }

    @NotNull
    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @NotNull
    @Override
    public EventId eventId() {
        return eventId;
    }

    public Set<ProjectChangeForEntity> getProjectChanges() {
        return changes;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
