package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NewRevisionsEventEmitterServiceImpl implements NewRevisionsEventEmitterService {

    private final EventDispatcher eventDispatcher;
    private final ProjectChangesManager projectChangesManager;

    public NewRevisionsEventEmitterServiceImpl(EventDispatcher eventDispatcher,
                                               ProjectChangesManager projectChangesManager) {
        this.eventDispatcher = eventDispatcher;
        this.projectChangesManager = projectChangesManager;
    }

    @Override
    public void emitNewRevisionsEvent(ProjectId projectId, List<EntityLinearizationHistory> entityLinearizationHistories) {
        Set<ProjectChangeForEntity> changeList = projectChangesManager.getProjectChangesForHistories(projectId, entityLinearizationHistories);
        NewRevisionsEvent revisionsEvent = NewRevisionsEvent.create(EventId.generate(), projectId, changeList);
        eventDispatcher.dispatchEvent(revisionsEvent);
    }

    @Override
    public void emitNewRevisionsEvent(ProjectId projectId, String whoficEntityIri, LinearizationRevision entityLinearizationRevision) {
        ProjectChangeForEntity projectChange = projectChangesManager.getProjectChangesForRevision(projectId, whoficEntityIri, entityLinearizationRevision);
        NewRevisionsEvent revisionsEvent = NewRevisionsEvent.create(EventId.generate(), projectId, Set.of(projectChange));
        eventDispatcher.dispatchEvent(revisionsEvent);
    }
}
