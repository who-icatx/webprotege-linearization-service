package edu.stanford.protege.webprotege.linearizationservice.repositories.history;

import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.UpdateResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.services.ReadWriteLockService;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationRevisionWithEntity;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.*;
import static edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRevision.TIMESTAMP;

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

    @Override
    public List<EntityLinearizationHistory> getAllEntityHistoriesForProjectId(ProjectId projectId) {
        Query query = new Query();

        query.addCriteria(
                Criteria.where(PROJECT_ID)
                        .is(projectId.value())
        );

        return mongoTemplate.find(query, EntityLinearizationHistory.class, LINEARIZATION_HISTORY_COLLECTION);
    }

    @Override
    public List<LinearizationRevisionWithEntity> getOrderedAndPagedHistoriesForProjectId(ProjectId projectId, int pageSize, int pageNumber) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(PROJECT_ID).is(projectId.value())),
                Aggregation.unwind(LINEARIZATION_REVISIONS),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, LINEARIZATION_REVISIONS + "." + TIMESTAMP)),
                Aggregation.skip(pageSize * (pageNumber - 1)),
                Aggregation.limit(pageSize)
        );

        return mongoTemplate.aggregate(aggregation, LINEARIZATION_HISTORY_COLLECTION, LinearizationRevisionWithEntity.class)
                .getMappedResults();
    }


    @Override
    public int getRevisionCountForProject(ProjectId projectId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(PROJECT_ID).is(projectId.value())),
                Aggregation.unwind(LINEARIZATION_REVISIONS),
                Aggregation.group().count().as("revisionCount")
        );

        Document result = mongoTemplate.aggregate(aggregation, LINEARIZATION_HISTORY_COLLECTION, Document.class)
                .getUniqueMappedResult();

        if (result != null && result.containsKey("revisionCount")) {
            return result.getInteger("revisionCount");
        } else {
            return 0;
        }
    }
}