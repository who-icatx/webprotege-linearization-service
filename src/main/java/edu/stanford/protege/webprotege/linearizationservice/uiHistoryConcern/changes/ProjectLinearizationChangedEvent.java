package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.EntityLinearizationChangeEvent.CHANNEL;


@JsonTypeName(CHANNEL)
public record ProjectLinearizationChangedEvent(EventId eventId,
                                       ProjectId projectId) implements ProjectEvent {
    public static final String CHANNEL = "webprotege.linearization.ProjectLinearizationChangedEvent";

    public static ProjectLinearizationChangedEvent create(EventId eventId,
                                                        ProjectId projectId) {
        return new ProjectLinearizationChangedEvent(eventId, projectId);
    }

    public ProjectLinearizationChangedEvent(EventId eventId, ProjectId projectId) {
        this.eventId = eventId;
        this.projectId = projectId;
    }

    public String getChannel() {
        return CHANNEL;
    }

    public EventId eventId() {
        return this.eventId;
    }

    public ProjectId projectId() {
        return this.projectId;
    }

}