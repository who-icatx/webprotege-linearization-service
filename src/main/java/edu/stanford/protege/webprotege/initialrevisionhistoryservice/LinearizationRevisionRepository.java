package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class LinearizationRevisionRepository {

    public final static String REVISION_HISTORY_COLLECTION = "EntityLinearizationHistories";

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    public LinearizationRevisionRepository(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }


    public EntityLinearizationHistory getExistingHistory(String entityIri, ProjectId projectId) {
        var query = query(where(WHOFIC_ENTITY_IRI_KEY).is(entityIri).and(PROJECT_ID).is(projectId).in(REVISION_HISTORY_COLLECTION));
        return mongoTemplate.findOne(query, EntityLinearizationHistory.class);
    }

    public void saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory) {
        mongoTemplate.save(entityLinearizationHistory, REVISION_HISTORY_COLLECTION);
    }

    public void saveAll(Set<EntityLinearizationHistory> historiesToBeSaved) {
        var collection = mongoTemplate.getCollection(REVISION_HISTORY_COLLECTION);
        /* THIS WORKS IF THE RECEIVED HISTORIES ARE UNIQUE */
        var documents = historiesToBeSaved.stream()
                .map(history -> new InsertOneModel<>(objectMapper.convertValue(history, Document.class)))
                .collect(Collectors.toList());

        collection.bulkWrite(documents);
    }

    public void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument){
        var collection = mongoTemplate.getCollection(REVISION_HISTORY_COLLECTION);
        collection.bulkWrite(listOfInsertOneModelDocument);
    }
}
