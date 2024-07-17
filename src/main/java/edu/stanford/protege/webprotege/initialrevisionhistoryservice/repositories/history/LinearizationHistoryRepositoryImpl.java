package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.*;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.result.UpdateResult;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.*;
import static java.util.stream.StreamSupport.stream;

@Repository
public class LinearizationHistoryRepositoryImpl implements LinearizationHistoryRepository {

    private final static String REVISION_HISTORY_COLLECTION = "EntityLinearizationHistories";

    @Autowired
    @Qualifier("enhancedMongoTemplate")
    private final MongoTemplate mongoTemplate;

    @Autowired
    private final ObjectMapper objectMapper;

    private final MongoMappingContext mongoMappingContext;
    private final MappingMongoConverter mappingMongoConverter;


    private final MongoClient mongoClient;

    public LinearizationHistoryRepositoryImpl(MongoTemplate mongoTemplate, ObjectMapper objectMapper, MongoMappingContext mongoMappingContext, MappingMongoConverter mappingMongoConverter, MongoClient mongoClient) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
        this.mongoMappingContext = mongoMappingContext;
        this.mappingMongoConverter = mappingMongoConverter;
        this.mongoClient = mongoClient;
    }

    @Override
    public EntityLinearizationHistory saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory) {
        return mongoTemplate.save(entityLinearizationHistory, REVISION_HISTORY_COLLECTION);
    }

    public void writeSingleHistory(EntityLinearizationHistory histories) {
        ClientSessionOptions sessionOptions = ClientSessionOptions.builder().causallyConsistent(true).build();

        ClientSession session = mongoClient.startSession(sessionOptions);
        var r = mongoTemplate.withSession(() -> session).execute(action -> {
            session.startTransaction();
            var result = action.insert(histories, REVISION_HISTORY_COLLECTION);
            session.commitTransaction();
            return result;
        }, ClientSession::close);

        System.out.println(r);

    }

    public void bulkWriteDocuments(List<InsertOneModel<Document>> listOfInsertOneModelDocument) {
        ClientSessionOptions sessionOptions = ClientSessionOptions.builder().causallyConsistent(true).build();

        ClientSession session = mongoClient.startSession(sessionOptions);
        var r = mongoTemplate.withSession(() -> session).execute(action -> {
            session.startTransaction();
            var result = action.insert(listOfInsertOneModelDocument, REVISION_HISTORY_COLLECTION);
            session.commitTransaction();
            return result;
        }, ClientSession::close);

        System.out.println(r);

    }

    @Override
    public void addRevision(IRI whoficEntityIri, ProjectId projectId, LinearizationRevision newRevision) {
        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(whoficEntityIri)
                .and(PROJECT_ID).is(projectId));

        Update update = new Update();
        update.push(LINEARIZATION_REVISIONS, newRevision);

        UpdateResult result = mongoTemplate.updateFirst(query, update, EntityLinearizationHistory.class, REVISION_HISTORY_COLLECTION);

        if (result.getMatchedCount() == 0) {
            throw new IllegalArgumentException(REVISION_HISTORY_COLLECTION + " not found for the given " +
                    WHOFIC_ENTITY_IRI + ":" + whoficEntityIri + " and " + PROJECT_ID +
                    ":" + projectId + ".");
        }
    }

    @Override
    public EntityLinearizationHistory findHistoryByEntityIriAndProjectId(IRI entityIri, ProjectId projectId) {

        EntityLinearizationHistory savedHistory;
        FindIterable<Document> documents = mongoTemplate.getCollection("EntityLinearizationHistories").find();
        Stream<Document> docs = stream(documents.spliterator(), false);

        Document doc = docs.findFirst().get();

        try {
            savedHistory = objectMapper.readValue(doc.toJson(), EntityLinearizationHistory.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return savedHistory;
    }

    @Override
    public EntityLinearizationHistory findWithSpringData(IRI entityIri, ProjectId projectId) {
        System.out.println("Retrieving EntityLinearizationHistory with IRI: " + entityIri + " and ProjectId: " + projectId);

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(entityIri.toString())
                .and(PROJECT_ID).is(projectId.value()));

        System.out.println("Actual query: " + query.toString());

        System.out.println("Mapped Fields: " + mappingMongoConverter.getMappingContext().getPersistentEntity(EntityLinearizationHistory.class));

        EntityLinearizationHistory result = mongoTemplate.findOne(query, EntityLinearizationHistory.class);
        System.out.println("Retrieved EntityLinearizationHistory: " + result);
        return result;
    }
}