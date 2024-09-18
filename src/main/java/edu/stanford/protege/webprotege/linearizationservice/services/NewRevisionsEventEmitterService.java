package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory;

import java.util.List;

public interface NewRevisionsEventEmitterService {
    void emitNewRevisionsEvent(ProjectId projectId, List<EntityLinearizationHistory> entityLinearizationHistories);
}
