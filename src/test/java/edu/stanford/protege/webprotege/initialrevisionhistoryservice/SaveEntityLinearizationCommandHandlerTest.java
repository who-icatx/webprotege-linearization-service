package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.LinearizationHistoryRepository;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.bson.Document;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
import static java.util.stream.StreamSupport.stream;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
public class SaveEntityLinearizationCommandHandlerTest extends IntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SaveEntityLinearizationCommandHandler commandHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LinearizationHistoryRepository repoCustom;

    @Before
    public void setUp() {
    }

    @Test
    public void GIVEN_entityWithNoLinearizationHistory_WHEN_savingNewEntityLinearization_THEN_createNewHistoryWithNewRevision() throws JsonProcessingException {
        var userId = UserId.valueOf("user1");
        var linearizationView = getRandomIri();
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        var executionContext = new ExecutionContext(userId, "jwt");
        LinearizationSpecification spec = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                linearizationParent,
                linearizationView,
                codingNote
        );

        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                residual,
                List.of(spec)
        );

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec), executionContext);

        var result = repoCustom.findHistoryByEntityIriAndProjectId(entityIri, projectId);

        Document filter = new Document("whoficEntityIri", entityIri);
        FindIterable<Document> documents = mongoTemplate.getCollection("EntityLinearizationHistories").find(filter);
        Stream<Document> docs = stream(documents.spliterator(), false);

        Document doc = docs.findFirst().get();
        EntityLinearizationHistory savedHistory = objectMapper.readValue(doc.toJson(), EntityLinearizationHistory.class);

        assertEquals(woficEntitySpec.entityIRI(), savedHistory.getWhoficEntityIri());

        var revision = savedHistory.getLinearizationRevisions().stream().toList().get(0);

        assertEquals(executionContext.userId(), revision.userId());
    }

    @Test
    public void GIVEN_entityWithLinearizationHistory_WHEN_savingEntityLinearization_THEN_createNewRevisionAndAddToExistingHistory() throws JsonProcessingException {
        var userId = UserId.valueOf("user1");
        var linearizationView = getRandomIri();
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        var executionContext = new ExecutionContext(userId, "jwt");

        LinearizationSpecification spec = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                linearizationParent,
                linearizationView,
                codingNote
        );

        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                residual,
                List.of(spec)
        );

        var existingHistory = getEntityLinearizationHistory(projectId, 2);

        mongoTemplate.insert(existingHistory);

    }
}