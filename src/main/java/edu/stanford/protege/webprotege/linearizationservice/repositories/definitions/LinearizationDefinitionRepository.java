package edu.stanford.protege.webprotege.linearizationservice.repositories.definitions;

import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.StreamSupport;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinition;

@Repository
public class LinearizationDefinitionRepository {
    public final static String DEFINITIONS_COLLECTION = "LinearizationDefinitions";

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    public LinearizationDefinitionRepository(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    public List<LinearizationDefinition> getLinearizationDefinitions() {
        return StreamSupport.stream(mongoTemplate.getCollection(DEFINITIONS_COLLECTION)
                                .find().spliterator(),
                        false
                )
                .map(doc -> objectMapper.convertValue(doc, LinearizationDefinition.class))
                .collect(toList());
    }
}