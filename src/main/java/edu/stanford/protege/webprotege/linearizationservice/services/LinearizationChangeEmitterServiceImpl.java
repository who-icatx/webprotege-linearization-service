package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinearizationChangeEmitterServiceImpl implements LinearizationChangeEmitterService {

    private final EventDispatcher eventDispatcher;

    public LinearizationChangeEmitterServiceImpl(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void emitLinearizationChangeEvent(ProjectId projectId, List<ProjectEvent> eventList) {
        if(!eventList.isEmpty()) {
            var packagedProjectChange = new PackagedProjectChangeEvent(projectId, EventId.generate(), eventList);
            eventDispatcher.dispatchEvent(packagedProjectChange);
        }
    }
}
