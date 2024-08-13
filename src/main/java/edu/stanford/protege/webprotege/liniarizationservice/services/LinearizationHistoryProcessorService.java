package edu.stanford.protege.webprotege.liniarizationservice.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.liniarizationservice.model.WhoficEntityLinearizationSpecification;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

public interface LinearizationHistoryProcessorService {
    Optional<WhoficEntityLinearizationSpecification> mergeLinearizationViewsFromParentsAndGetDefaultSpec(IRI currenteEtityIri, Set<IRI> parentEntityIris, ProjectId projectId);
}
