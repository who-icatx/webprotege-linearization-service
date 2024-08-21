package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.EntityLinearizationChangeEvent.CHANNEL;

@JsonTypeName(CHANNEL)
public record EntityLinearizationChangeEvent(EventId eventId,
                                             ProjectId projectId,
                                             String whoficEntityIri) implements ProjectEvent {
    public static final String CHANNEL = "webprotege.linearization.LinearizationChanged";

    public static EntityLinearizationChangeEvent create(EventId eventId,
                                                        ProjectId projectId,
                                                        String whoficEntityIri) {
        return new EntityLinearizationChangeEvent(eventId, projectId, whoficEntityIri);
    }

    public EntityLinearizationChangeEvent(EventId eventId, ProjectId projectId, String whoficEntityIri) {
        this.eventId = eventId;
        this.projectId = projectId;
        this.whoficEntityIri = whoficEntityIri;
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

    public String whoficEntityIri() {
        return this.whoficEntityIri;
    }
}