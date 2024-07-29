package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.*;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ExtendWith({SpringExtension.class, IntegrationTest.class})
@ActiveProfiles("test")
public class SaveEntityLinearizationCommandHandlerTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SaveEntityLinearizationCommandHandler commandHandler;

    @Test
    public void GIVEN_entityWithNoLinearizationHistory_WHEN_savingNewEntityLinearization_THEN_createNewHistoryWithNewRevision() {
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

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(entityIri,
                residual,
                List.of(spec)
        );

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec), executionContext);

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(entityIri)
                .and(PROJECT_ID).is(projectId.value()));

        var newHistory = mongoTemplate.findOne(query, EntityLinearizationHistory.class);

        assertNotNull(newHistory);

        assertEquals(woficEntitySpec.entityIRI().toString(), newHistory.getWhoficEntityIri());

        var revisions = newHistory.getLinearizationRevisions().stream().toList();

        assertEquals(1, revisions.size());

        var revision = revisions.get(0);

        assertEquals(executionContext.userId(), revision.userId());

        var revisionsIsIncludedEvent = revision.linearizationEvents().stream().filter(event -> event instanceof SetIncludedInLinearization).findFirst();

        assertTrue(revisionsIsIncludedEvent.isPresent());

        assertEquals(spec.getIsIncludedInLinearization().name(), revisionsIsIncludedEvent.get().getValue());

        var revisionsResiduals = revision.linearizationEvents().stream().filter(event -> event instanceof SetUnspecifiedResidualTitle).findFirst();

        assertTrue(revisionsResiduals.isPresent());

        assertEquals(residual.getUnspecifiedResidualTitle(), revisionsResiduals.get().getValue());
    }

    @Test
    public void GIVEN_entityWithLinearizationHistory_WHEN_savingEntityLinearization_THEN_createNewRevisionAndAddToExistingHistory() {
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
                linearizationParent,
                linearizationView,
                codingNote
        );

        var residual1 = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());

        var woficEntitySpec1 = new WhoficEntityLinearizationSpecification(
                entityIri,
                residual1,
                List.of(spec1)
        );

        LinearizationSpecification spec2 = new LinearizationSpecification(
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.TRUE,
                linearizationParent,
                linearizationView,
                codingNote
        );

        var residual2 = new LinearizationResiduals(ThreeStateBoolean.TRUE, getRandomString());

        var woficEntitySpec2 = new WhoficEntityLinearizationSpecification(
                entityIri,
                residual2,
                List.of(spec2)
        );

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec1), executionContext);

        commandHandler.handleRequest(new SaveEntityLinearizationRequest(projectId, woficEntitySpec2), executionContext);

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(entityIri)
                .and(PROJECT_ID).is(projectId.value()));

        var newHistory = mongoTemplate.findOne(query, EntityLinearizationHistory.class);

        assertNotNull(newHistory);

        assertEquals(woficEntitySpec1.entityIRI().toString(), newHistory.getWhoficEntityIri());

        var revisions = newHistory.getLinearizationRevisions().stream().toList();

        assertEquals(2, revisions.size());

        var revision2 = revisions.get(1);

        assertEquals(executionContext.userId(), revision2.userId());

        var revisions2IsIncludedEvent = revision2.linearizationEvents().stream().filter(event -> event instanceof SetIncludedInLinearization).findFirst();

        assertTrue(revisions2IsIncludedEvent.isPresent());

        assertEquals(spec2.getIsIncludedInLinearization().name(), revisions2IsIncludedEvent.get().getValue());

        var revisions2Residuals = revision2.linearizationEvents().stream().filter(event -> event instanceof SetUnspecifiedResidualTitle).findFirst();

        assertTrue(revisions2Residuals.isPresent());

        assertEquals(residual2.getUnspecifiedResidualTitle(), revisions2Residuals.get().getValue());
    }
}