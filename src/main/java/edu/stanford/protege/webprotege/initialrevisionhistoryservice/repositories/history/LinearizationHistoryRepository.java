package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;

public interface LinearizationHistoryRepository {

    EntityLinearizationHistory saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory);

    void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument);

    void addRevision(IRI whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision);

    EntityLinearizationHistory findHistoryByEntityIriAndProjectId(IRI entityIri, ProjectId projectId);

    EntityLinearizationHistory findWithSpringData(IRI entityIri, ProjectId projectId);
    void writeSingleHistory(EntityLinearizationHistory histories);
}
