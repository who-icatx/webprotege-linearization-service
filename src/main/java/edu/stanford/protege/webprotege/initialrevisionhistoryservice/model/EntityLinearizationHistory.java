package edu.stanford.protege.webprotege.initialrevisionhistoryservice.model;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;

import java.util.Set;

public record EntityLinearizationHistory(IRI whoficEntityIri,
                                         ProjectId projectId,
                                         Set<LinearizationRevision> linearizationRevisions) {

    public static final String WHOFIC_ENTITY_IRI_KEY = "whoficEntityIri";

    public static final String PROJECT_ID = "projectId";

}
