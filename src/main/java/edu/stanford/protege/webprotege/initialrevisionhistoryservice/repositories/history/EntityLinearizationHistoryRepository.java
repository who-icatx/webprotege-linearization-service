package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntityLinearizationHistoryRepository extends MongoRepository<EntityLinearizationHistory, String>, LinearizationHistoryRepositoryCustom{
    EntityLinearizationHistory findByWhoficEntityIriAndProjectId(IRI whoficEntityIri, ProjectId projectId);
}
