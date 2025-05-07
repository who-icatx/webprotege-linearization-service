package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.change.ProjectChange;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.EntityType;

public record ProjectChangeForEntity(String whoficEntityIri,
                                     ChangeType changeType,
                                     EntityType entityType,
                                     ProjectChange projectChange) implements Comparable<ProjectChangeForEntity> {

    public static ProjectChangeForEntity create(String whoficEntityIri,
                                                ProjectChange projectChange) {
        return new ProjectChangeForEntity(whoficEntityIri, ChangeType.UPDATE_ENTITY, EntityType.CLASS, projectChange);
    }

    @Override
    public int compareTo(@NotNull ProjectChangeForEntity other) {
        int timestampComparison = Long.compare(this.projectChange.getTimestamp(), other.projectChange.getTimestamp());
        if (timestampComparison != 0) {
            return timestampComparison;
        }
        return this.whoficEntityIri.compareTo(other.whoficEntityIri);
    }
}
