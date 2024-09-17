package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationRevisionWithEntity;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.function.Consumer;

public interface LinearizationHistoryService {
    void saveMultipleEntityLinearizationHistories(Set<EntityLinearizationHistory> historiesToBeSaved);

    Optional<EntityLinearizationHistory> getExistingHistoryOrderedByRevision(IRI entityIri, ProjectId projectId);

    void addRevision(WhoficEntityLinearizationSpecification linearizationSpecification,
                     ProjectId projectId, UserId userId);

    Consumer<List<WhoficEntityLinearizationSpecification>> createBatchProcessorForSavingPaginatedHistories(ProjectId projectId, UserId userId);

    List<EntityLinearizationHistory> getAllExistingHistoriesForProject(ProjectId projectId);

    List<LinearizationRevisionWithEntity> getAllExistingHistoriesForProjectWithPageAndPageSize(ProjectId projectId, int page, int pageSize);

    int getRevisionCountForProject(ProjectId projectId);
}
