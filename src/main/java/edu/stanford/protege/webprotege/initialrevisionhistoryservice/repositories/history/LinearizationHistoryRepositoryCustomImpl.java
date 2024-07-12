package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.UpdateResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;

import java.util.List;

public class LinearizationHistoryRepositoryCustomImpl implements LinearizationHistoryRepositoryCustom {

    private final static String REVISION_HISTORY_COLLECTION = "EntityLinearizationHistories";
    private final static String WOFIC_ENTITY_IRI_PARAMETER = "whoficEntityIri";
    private final static String PROJECT_ID_PARAMETER = "projectId";
    private final static String LINEARIZATION_REVISIONS_PARAMETER = "linearizationRevisions";

    private final MongoTemplate mongoTemplate;


    public LinearizationHistoryRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory) {
        mongoTemplate.save(entityLinearizationHistory, REVISION_HISTORY_COLLECTION);
    }

    public void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument) {
        var collection = mongoTemplate.getCollection(REVISION_HISTORY_COLLECTION);
        collection.bulkWrite(listOfInsertOneModelDocument);
    }

    @Override
    public void addRevision(IRI whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision) {
        Query query = new Query();
        query.addCriteria(Criteria.where(WOFIC_ENTITY_IRI_PARAMETER).is(whoficEntityIri)
                .and(PROJECT_ID_PARAMETER).is(projectId));

        Update update = new Update();
        update.push(LINEARIZATION_REVISIONS_PARAMETER, newRevision);

        UpdateResult result = mongoTemplate.updateFirst(query, update, EntityLinearizationHistory.class);

        if (result.getMatchedCount() == 0) {
            throw new IllegalArgumentException(REVISION_HISTORY_COLLECTION + " not found for the given " +
                    WOFIC_ENTITY_IRI_PARAMETER + ":" + whoficEntityIri + " and " + PROJECT_ID_PARAMETER +
                    ":" + projectId + ".");
        }
    }
}