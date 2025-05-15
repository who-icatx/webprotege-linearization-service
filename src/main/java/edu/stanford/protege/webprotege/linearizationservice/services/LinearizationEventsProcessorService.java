package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.model.*;

import java.util.Set;

public interface LinearizationEventsProcessorService {
    WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory, ExecutionContext executionContext);
    WhoficEntityLinearizationSpecification processHistory(Set<LinearizationRevision> linearizationRevisions,ProjectId projectId, ExecutionContext executionContext, String entityIri);
}
