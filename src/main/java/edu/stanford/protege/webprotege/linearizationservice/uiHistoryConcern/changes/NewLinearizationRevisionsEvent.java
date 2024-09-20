package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.NewLinearizationRevisionsEvent.CHANNEL;


@JsonTypeName(CHANNEL)
public record NewLinearizationRevisionsEvent(
        EventId eventId,
        ProjectId projectId,
        List<ProjectChangeForEntity> changeList
) implements ProjectEvent {
    public final static String CHANNEL = "webprotege.events.projects.linearizations.NewLinearizationRevisionsEvent";

    public static NewLinearizationRevisionsEvent create(EventId eventId,
                                                        ProjectId projectId,
                                                        List<ProjectChangeForEntity> changeList) {
        return new NewLinearizationRevisionsEvent(eventId, projectId, changeList);
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

    public List<ProjectChangeForEntity> getProjectChanges() {
        return changeList;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
