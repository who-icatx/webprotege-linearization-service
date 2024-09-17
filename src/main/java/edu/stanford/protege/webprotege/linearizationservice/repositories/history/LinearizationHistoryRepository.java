package edu.stanford.protege.webprotege.linearizationservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationRevisionWithEntity;
import org.bson.Document;

import java.util.*;

public interface LinearizationHistoryRepository {

    EntityLinearizationHistory saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory);

    void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument);

    void addRevision(String whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision);

    Optional<EntityLinearizationHistory> findHistoryByEntityIriAndProjectId(String entityIri, ProjectId projectId);

    List<EntityLinearizationHistory> getAllEntityHistoriesForProjectId(ProjectId projectId);

    List<LinearizationRevisionWithEntity> getOrderedAndPagedHistoriesForProjectId(ProjectId projectId, int pageSize, int pageNumber);

    int getRevisionCountForProject(ProjectId projectId);
}
