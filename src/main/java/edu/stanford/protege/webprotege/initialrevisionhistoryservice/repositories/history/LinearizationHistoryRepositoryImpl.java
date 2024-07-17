package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.UpdateResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.*;

@Repository
public class LinearizationHistoryRepositoryImpl implements LinearizationHistoryRepository {

    private final static String REVISION_HISTORY_COLLECTION = "EntityLinearizationHistories";

    @Autowired
    private final MongoTemplate mongoTemplate;

    public LinearizationHistoryRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public EntityLinearizationHistory saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory) {
        return mongoTemplate.save(entityLinearizationHistory, REVISION_HISTORY_COLLECTION);
    }

    @Override
    public void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument) {
        var collection = mongoTemplate.getCollection(REVISION_HISTORY_COLLECTION);
        collection.bulkWrite(listOfInsertOneModelDocument);
    }

    @Override
    public void addRevision(String whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision) {
        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(whoficEntityIri)
                .and(PROJECT_ID).is(projectId.id()));

        Update update = new Update();
        update.push(LINEARIZATION_REVISIONS, newRevision);

        UpdateResult result = mongoTemplate.updateFirst(query, update, EntityLinearizationHistory.class);

        if (result.getMatchedCount() == 0) {
            throw new IllegalArgumentException(REVISION_HISTORY_COLLECTION + " not found for the given " +
                    WHOFIC_ENTITY_IRI + ":" + whoficEntityIri + " and " + PROJECT_ID +
                    ":" + projectId + ".");
        }
    }

    @Override
    public EntityLinearizationHistory findHistoryByEntityIriAndProjectId(String entityIri, ProjectId projectId) {

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(entityIri)
                .and(PROJECT_ID).is(projectId.value()));

        return mongoTemplate.findOne(query, EntityLinearizationHistory.class);
    }
}