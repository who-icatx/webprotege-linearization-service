package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;

public interface LinearizationEventsProcessorService {
    WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory);
}
