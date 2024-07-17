package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.SetIncludedInLinearization;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.LinearizationHistoryRepository;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.junit.*;
import org.junit.runner.RunWith;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
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
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec), executionContext);

        var newHistory = repoCustom.findHistoryByEntityIriAndProjectId(entityIri, projectId);

        assertEquals(woficEntitySpec.entityIRI().toString(), newHistory.getWhoficEntityIri());

        var revisions = newHistory.getLinearizationRevisions().stream().toList();

        assertEquals(1, revisions.size());

        var revision = revisions.get(0);

        assertEquals(executionContext.userId(), revision.userId());
    }

    @Test
    public void GIVEN_entityWithLinearizationHistory_WHEN_savingEntityLinearization_THEN_createNewRevisionAndAddToExistingHistory(){
        var userId = UserId.valueOf("user1");
        var linearizationView = getRandomIri();
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        var executionContext = new ExecutionContext(userId, "jwt");
        LinearizationSpecification spec1 = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        var residual1 = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());

        var woficEntitySpec1 = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual1,
                List.of(spec1)
        );

        LinearizationSpecification spec2 = new LinearizationSpecification(
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.TRUE,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        var residual2 = new LinearizationResiduals(ThreeStateBoolean.TRUE, getRandomString());

        var woficEntitySpec2 = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual2,
                List.of(spec2)
        );

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec1), executionContext);

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec2), executionContext);

        var newHistory = repoCustom.findHistoryByEntityIriAndProjectId(entityIri, projectId);

        assertEquals(woficEntitySpec1.entityIRI().toString(), newHistory.getWhoficEntityIri());

        var revisions = newHistory.getLinearizationRevisions().stream().toList();

        assertEquals(2, revisions.size());

        var revision2 = revisions.get(1);

        assertEquals(executionContext.userId(), revision2.userId());

        var revisions2IsIncludedEvent = revision2.linearizationEvents().stream().filter(event -> event instanceof SetIncludedInLinearization).findFirst();
        assertEquals(spec2.getIsIncludedInLinearization().name(), revisions2IsIncludedEvent.get().getValue());
    }
}