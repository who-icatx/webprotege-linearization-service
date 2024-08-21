package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.*;

import java.util.List;

public interface LinearizationChangeEmitterService {
    void emitLinearizationChangeEvent(ProjectId projectId, List<ProjectEvent> eventList);
}
