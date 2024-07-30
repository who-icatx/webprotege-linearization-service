package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;

import java.util.Set;

public interface LinearizationEventsProcessorService {
    WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory);
    WhoficEntityLinearizationSpecification processHistory(Set<LinearizationRevision> linearizationRevisions, String entityIri);
}
