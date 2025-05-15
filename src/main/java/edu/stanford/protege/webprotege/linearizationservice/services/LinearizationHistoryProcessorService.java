package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

public interface LinearizationHistoryProcessorService {
    Optional<WhoficEntityLinearizationSpecification> mergeLinearizationViewsFromParentsAndGetDefaultSpec(IRI currenteEtityIri, ExecutionContext executionContext, Set<IRI> parentEntityIris, ProjectId projectId);
}
