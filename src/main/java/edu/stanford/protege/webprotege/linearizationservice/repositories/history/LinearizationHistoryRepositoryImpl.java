package edu.stanford.protege.webprotege.linearizationservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.UpdateResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.services.ReadWriteLockService;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.*;

@Repository
public class LinearizationHistoryRepositoryImpl implements LinearizationHistoryRepository {

    private final MongoTemplate mongoTemplate;
    private final ReadWriteLockService readWriteLock;

    public LinearizationHistoryRepositoryImpl(MongoTemplate mongoTemplate, ReadWriteLockService readWriteLock) {
        this.mongoTemplate = mongoTemplate;
        this.readWriteLock = readWriteLock;
    }

    @Override
    public EntityLinearizationHistory saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory) {
        return readWriteLock.executeWriteLock(() -> mongoTemplate.save(entityLinearizationHistory, LINEARIZATION_HISTORY_COLLECTION));
    }

    @Override
    public void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument) {
        readWriteLock.executeWriteLock(() -> {
            var collection = mongoTemplate.getCollection(LINEARIZATION_HISTORY_COLLECTION);
            collection.bulkWrite(listOfInsertOneModelDocument);
        });
    }

    @Override
    public void addRevision(String whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision) {
        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(whoficEntityIri)
                .and(PROJECT_ID).is(projectId.id()));

        Update update = new Update();
        update.push(LINEARIZATION_REVISIONS, newRevision);

        readWriteLock.executeWriteLock(() -> {
            UpdateResult result = mongoTemplate.updateFirst(query, update, EntityLinearizationHistory.class, LINEARIZATION_HISTORY_COLLECTION);
            if (result.getMatchedCount() == 0) {
                throw new IllegalArgumentException(LINEARIZATION_HISTORY_COLLECTION + " not found for the given " +
                        WHOFIC_ENTITY_IRI + ":" + whoficEntityIri + " and " + PROJECT_ID +
                        ":" + projectId + ".");
            }
        });
    }

    @Override
    public Optional<EntityLinearizationHistory> findHistoryByEntityIriAndProjectId(String entityIri, ProjectId projectId) {

        Query query = new Query();
        query.addCriteria(
                Criteria.where(WHOFIC_ENTITY_IRI).is(entityIri)
                        .and(PROJECT_ID).is(projectId.value())
        );

        return readWriteLock.executeReadLock(() -> Optional.ofNullable(mongoTemplate.findOne(query, EntityLinearizationHistory.class, LINEARIZATION_HISTORY_COLLECTION)));
    }
}