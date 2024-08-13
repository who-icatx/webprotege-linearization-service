package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.linearizationservice.model.*;

import java.util.Set;

public interface LinearizationEventsProcessorService {
    WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory);
    WhoficEntityLinearizationSpecification processHistory(Set<LinearizationRevision> linearizationRevisions, String entityIri);
}
