package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;

import java.util.List;

public interface LinearizationHistoryRepository {

    EntityLinearizationHistory saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory);

    void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument);

    void addRevision(String whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision);

    EntityLinearizationHistory findHistoryByEntityIriAndProjectId(String entityIri, ProjectId projectId);
}
