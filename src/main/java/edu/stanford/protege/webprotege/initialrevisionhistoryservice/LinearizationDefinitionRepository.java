package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationDefinition;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Repository
public class LinearizationDefinitionRepository {
    public final static String REVISION_HISTORY_COLLECTION = "LinearizationDefinitions";

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    public LinearizationDefinitionRepository(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    public List<LinearizationDefinition> getLinearizationDefinitions() {
        return StreamSupport.stream(mongoTemplate.getCollection(REVISION_HISTORY_COLLECTION)
                                .find().spliterator(),
                        false
                )
                .map(doc -> objectMapper.convertValue(doc, LinearizationDefinition.class))
                .collect(toList());
    }
}
