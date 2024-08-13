package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.linearizationservice.*;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.junit.jupiter.api.*;
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

import java.util.*;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.*;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ExtendWith({SpringExtension.class, IntegrationTest.class})
@ActiveProfiles("test")
class MergeWithParentEntitiesCommandHandlerTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private LinearizationEventMapper eventMapper;

    @Autowired
    private MergeWithParentEntitiesCommandHandler commandHandler;

    @Test
    void GIVEN_requestWithTwoNewParentsWithNewViews_WHEN_processingRequest_THEN_addNewViewsToEntityLinearization() {
        IRI currenteEtityIri = IRI.create(getRandomIri());
        IRI parentEntityIri1 = IRI.create(getRandomIri());
        IRI parentEntityIri2 = IRI.create(getRandomIri());
        Set<IRI> parentEntityIris = Set.of(parentEntityIri1, parentEntityIri2);
        ProjectId projectId = ProjectId.generate();
        UserId userId = UserId.valueOf(getRandomString());
        var executionContext = new ExecutionContext(userId, "jwt");

        LinearizationSpecification currSpec1 = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.TRUE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/entity/MMS"),
                "");
        LinearizationSpecification currSpec2 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/entity/primCareLowResSet"),
                "");
        WhoficEntityLinearizationSpecification currentSpec = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec1, currSpec2));

        Set<LinearizationEvent> linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(currentSpec);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(currentSpec));

        var currEntityRevision = LinearizationRevision.create(userId, linearizationEvents);
        var currEntityHistory = EntityLinearizationHistory.create(currenteEtityIri.toString(), projectId.id(), Set.of(currEntityRevision));

        mongoTemplate.save(currEntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        LinearizationSpecification missingSpec1 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/entity/research"),
                "");
        WhoficEntityLinearizationSpecification parentWhoficSpec1 = new WhoficEntityLinearizationSpecification(parentEntityIri1, null, List.of(missingSpec1));

        Set<LinearizationEvent> parent1Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec1);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec1));

        var parent1EntityRevision = LinearizationRevision.create(userId, parent1Events);
        var parent1EntityHistory = EntityLinearizationHistory.create(parentEntityIri1.toString(), projectId.id(), Set.of(parent1EntityRevision));

        mongoTemplate.save(parent1EntityHistory, LINEARIZATION_HISTORY_COLLECTION);


        LinearizationSpecification missingSpec2 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/entity/mentalHealth"),
                "");

        WhoficEntityLinearizationSpecification parentWhoficSpec2 = new WhoficEntityLinearizationSpecification(parentEntityIri2, null, List.of(missingSpec2));

        Set<LinearizationEvent> parent2Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec2);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec2));

        var parent2EntityRevision = LinearizationRevision.create(userId, parent2Events);
        var parent2EntityHistory = EntityLinearizationHistory.create(parentEntityIri2.toString(), projectId.id(), Set.of(parent2EntityRevision));
        mongoTemplate.save(parent2EntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        commandHandler.handleRequest(MergeWithParentEntitiesRequest.create(currenteEtityIri, parentEntityIris, projectId), executionContext);

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(currenteEtityIri.toString())
                .and(PROJECT_ID).is(projectId.value()));

        var newHistory = mongoTemplate.findOne(query, EntityLinearizationHistory.class, LINEARIZATION_HISTORY_COLLECTION);

        assertNotNull(newHistory);
        assertNotNull(newHistory.getLinearizationRevisions());
        assertEquals(2, newHistory.getLinearizationRevisions().size());

        var newEntityRevision = newHistory.getLinearizationRevisions().stream().toList();
        var lastEntityRevision = newEntityRevision.get(newEntityRevision.size() - 1);

        lastEntityRevision.linearizationEvents()
                .stream()
                .filter(event -> event instanceof LinearizationSpecificationEvent)
                .map(event -> (LinearizationSpecificationEvent) event)
                .flatMap(spec -> {
                            var newSpecFromParent1Added = parent1Events.stream()
                                    .filter(event -> event instanceof LinearizationSpecificationEvent)
                                    .map(event -> (LinearizationSpecificationEvent) event)
                                    .anyMatch(parentSpec -> parentSpec.getLinearizationView().equals(spec.getLinearizationView()));
                            var newSpecFromParent2Added = parent2Events.stream()
                                    .filter(event -> event instanceof LinearizationSpecificationEvent)
                                    .map(event -> (LinearizationSpecificationEvent) event)
                                    .anyMatch(parentSpec -> parentSpec.getLinearizationView().equals(spec.getLinearizationView()));
                            return Stream.of(newSpecFromParent1Added || newSpecFromParent2Added);
                        }
                ).forEach(Assertions::assertTrue);
    }

    @Test
    void GIVEN_requestWithTwoNewParentsSameViewsAsChild_WHEN_processingRequest_THEN_noRevisionAddedToEntityLinearization() {
        IRI currenteEtityIri = IRI.create(getRandomIri());
        IRI parentEntityIri1 = IRI.create(getRandomIri());
        IRI parentEntityIri2 = IRI.create(getRandomIri());
        Set<IRI> parentEntityIris = Set.of(parentEntityIri1, parentEntityIri2);
        ProjectId projectId = ProjectId.generate();
        UserId userId = UserId.valueOf(getRandomString());
        var executionContext = new ExecutionContext(userId, "jwt");

        LinearizationSpecification currSpec1 = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.TRUE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/entity/MMS"),
                "");
        LinearizationSpecification currSpec2 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/entity/primCareLowResSet"),
                "");
        WhoficEntityLinearizationSpecification currentSpec = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec1, currSpec2));

        Set<LinearizationEvent> linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(currentSpec);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(currentSpec));

        var currEntityRevision = LinearizationRevision.create(userId, linearizationEvents);
        var currEntityHistory = EntityLinearizationHistory.create(currenteEtityIri.toString(), projectId.id(), Set.of(currEntityRevision));

        mongoTemplate.save(currEntityHistory, LINEARIZATION_HISTORY_COLLECTION);


        WhoficEntityLinearizationSpecification parentWhoficSpec1 = new WhoficEntityLinearizationSpecification(parentEntityIri1, null, List.of(currSpec1));

        Set<LinearizationEvent> parent1Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec1);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec1));

        var parent1EntityRevision = LinearizationRevision.create(userId, parent1Events);
        var parent1EntityHistory = EntityLinearizationHistory.create(parentEntityIri1.toString(), projectId.id(), Set.of(parent1EntityRevision));

        mongoTemplate.save(parent1EntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        WhoficEntityLinearizationSpecification parentWhoficSpec2 = new WhoficEntityLinearizationSpecification(parentEntityIri2, null, List.of(currSpec2));

        Set<LinearizationEvent> parent2Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec2);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec2));

        var parent2EntityRevision = LinearizationRevision.create(userId, parent2Events);
        var parent2EntityHistory = EntityLinearizationHistory.create(parentEntityIri2.toString(), projectId.id(), Set.of(parent2EntityRevision));
        mongoTemplate.save(parent2EntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        commandHandler.handleRequest(MergeWithParentEntitiesRequest.create(currenteEtityIri, parentEntityIris, projectId), executionContext);

        Query query = new Query();
        query.addCriteria(Criteria.where(WHOFIC_ENTITY_IRI).is(currenteEtityIri.toString())
                .and(PROJECT_ID).is(projectId.value()));

        var newHistory = mongoTemplate.findOne(query, EntityLinearizationHistory.class, LINEARIZATION_HISTORY_COLLECTION);

        assertNotNull(newHistory);
        assertNotNull(newHistory.getLinearizationRevisions());
        assertEquals(1, newHistory.getLinearizationRevisions().size());
    }
}