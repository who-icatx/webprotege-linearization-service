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

import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.*;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistoryForEntityIri;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.getRandomIri;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ExtendWith({SpringExtension.class, IntegrationTest.class})
@ActiveProfiles("test")
class CreateLinearizationFromParentCommandHandlerTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CreateLinearizationFromParentCommandHandler commandHandler;

    @Test
    void GIVEN_parentWithLinearizationHistory_WHEN_creatingChildOfParent_THEN_alsoCreatInitialRevisionUsingParentHistoryWithDefaultValues() {

        var userId = UserId.valueOf("user1");
        var parentIri = getRandomIri();
        var newEntityIri = getRandomIri();
        var projectId = ProjectId.generate();
        var executionContext = new ExecutionContext(userId, "jwt");

        var parentEntityHistory = getEntityLinearizationHistoryForEntityIri(parentIri, projectId, 3);
        mongoTemplate.save(parentEntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        commandHandler.handleRequest(new CreateLinearizationFromParentRequest(IRI.create(newEntityIri), IRI.create(parentIri), projectId), executionContext);

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(newEntityIri)
                .and(PROJECT_ID).is(projectId.value()));

        var newHistory = mongoTemplate.findOne(query, EntityLinearizationHistory.class);

        assertNotNull(newHistory);
        assertNotNull(newHistory.getLinearizationRevisions());
        assertEquals(1, newHistory.getLinearizationRevisions().size());

        var newEntityRevision = newHistory.getLinearizationRevisions().stream().toList().get(0);

        /*
        ToDo:
            improve this check so we don't need to change it every time the default spec changes.
         */
        //here we are expecting the default values. If the default values change we need to also update this test.
        newEntityRevision.linearizationEvents().forEach(event -> {
            if (event instanceof SetAuxiliaryAxisChild) {
                assertEquals(ThreeStateBoolean.FALSE.name(), event.getValue());
            } else if (event instanceof SetCodingNote) {
                assertEquals("", event.getValue());
            } else if (event instanceof SetGrouping) {
                assertEquals(ThreeStateBoolean.FALSE.name(), event.getValue());
            } else if (event instanceof SetIncludedInLinearization) {
                assertEquals(ThreeStateBoolean.UNKNOWN.name(), event.getValue());
            } else if (event instanceof SetLinearizationParent) {
                assertEquals("", event.getValue());
            } else if (event instanceof SetSuppressedSpecifiedResidual) {
                assertEquals(ThreeStateBoolean.UNKNOWN.name(), event.getValue());
            } else if (event instanceof SetUnspecifiedResidualTitle) {
                assertEquals("", event.getValue());
            }
        });

        parentEntityHistory.getLinearizationRevisions()
                .stream()
                .flatMap(revision -> Stream.of(revision.linearizationEvents()))
                .filter(parentEvent -> parentEvent instanceof LinearizationSpecificationEvent)
                .flatMap(parentEvent -> Stream.of((LinearizationSpecificationEvent) parentEvent))
                .forEach(parentEvent -> {
                            var parentViewExistsInChild = newEntityRevision.linearizationEvents()
                                    .stream()
                                    .filter(childEntityEvent -> childEntityEvent instanceof LinearizationSpecificationEvent)
                                    .flatMap(childEntityEvent -> Stream.of((LinearizationSpecificationEvent) childEntityEvent))
                                    .anyMatch(childEntityEvent -> childEntityEvent.getLinearizationView().equals(parentEvent.getLinearizationView()));

                            assertTrue(parentViewExistsInChild);
                        }
                );


        parentEntityHistory.getLinearizationRevisions()
                .stream()
                .flatMap(revision -> Stream.of(revision.linearizationEvents()))
                .filter(parentEvent -> !(parentEvent instanceof LinearizationSpecificationEvent))
                .forEach(parentEvent -> {
                            if (parentEvent instanceof SetUnspecifiedResidualTitle) {
                                var residualExistsInChild = newEntityRevision.linearizationEvents()
                                        .stream()
                                        .anyMatch(childEntityEvent -> (childEntityEvent instanceof SetUnspecifiedResidualTitle));

                                assertTrue(residualExistsInChild);
                            } else if (parentEvent instanceof SetSuppressedSpecifiedResidual) {
                                var residualExistsInChild = newEntityRevision.linearizationEvents()
                                        .stream()
                                        .anyMatch(childEntityEvent -> (childEntityEvent instanceof SetSuppressedSpecifiedResidual));

                                assertTrue(residualExistsInChild);
                            }
                        }
                );
    }
}