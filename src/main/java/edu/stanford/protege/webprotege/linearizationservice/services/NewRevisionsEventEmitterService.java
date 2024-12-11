package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.*;

import java.util.List;

public interface NewRevisionsEventEmitterService {
    void emitNewRevisionsEvent(ProjectId projectId, List<EntityLinearizationHistory> entityLinearizationHistories, ChangeRequestId changeRequestId);
    void emitNewRevisionsEvent(ProjectId projectId, String whoficEntityIri, LinearizationRevision entityLinearizationRevision, ChangeRequestId changeRequestId);
}
