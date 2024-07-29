package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

public interface LinearizationHistoryProcessorService {
    Optional<WhoficEntityLinearizationSpecification> mergeLinearizationViewsFromParentsAndGetDefaultSpec(IRI currenteEtityIri, Set<IRI> parentEntityIris, ProjectId projectId);
}
