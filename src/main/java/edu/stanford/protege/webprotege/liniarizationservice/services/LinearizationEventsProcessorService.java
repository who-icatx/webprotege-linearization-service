package edu.stanford.protege.webprotege.liniarizationservice.services;

import edu.stanford.protege.webprotege.liniarizationservice.model.*;

import java.util.Set;

public interface LinearizationEventsProcessorService {
    WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory);
    WhoficEntityLinearizationSpecification processHistory(Set<LinearizationRevision> linearizationRevisions, String entityIri);
}
