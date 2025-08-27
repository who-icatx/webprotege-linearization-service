package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import edu.stanford.protege.webprotege.criteria.MultiMatchType;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.*;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.mappers.LinearizationEventMapper;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationViewIriHelper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.*;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @MockBean
    private CommandExecutor<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> getAuthorizedActionsExecutor;

    @MockBean
    private CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor;

    @MockBean
    private LinearizationDefinitionRepository definitionRepository;


    @BeforeEach
    public  void setUp() throws IOException {
        Set<Capability> capabilities = new HashSet<>();
        LinearizationRowsCapability capability = new LinearizationRowsCapability(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW,
                LinearizationViewIriHelper.getLinearizationViewIris().stream().map(IRI::toString).collect(Collectors.toList()),
                CompositeRootCriteria.get(new ArrayList<>(), MultiMatchType.ANY));
        capabilities.add(capability);
        when(getAuthorizedActionsExecutor.execute(any(), any())).thenReturn(CompletableFuture
                .supplyAsync(() -> new GetAuthorizedCapabilitiesResponse(new ProjectResource(ProjectId.generate()),
                        Subject.forUser(new UserId("user1")), capabilities)));
        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/LinearizationDefinitions.json");
        when(definitionRepository.getLinearizationDefinitions())
                .thenReturn(objectMapper.readValue(fileInputStream, new TypeReference<>() {
                }));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.supplyAsync(() -> new GetMatchingCriteriaResponse(Arrays.asList(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW))));
    }

    @Test
    void GIVEN_requestWithTwoNewParentsWithNewViews_WHEN_processingRequest_THEN_addNewViewsToEntityLinearization() {
        IRI currenteEtityIri = IRI.create(getRandomIri());
        IRI parentEntityIri1 = IRI.create(getRandomIri());
        IRI parentEntityIri2 = IRI.create(getRandomIri());
        Set<IRI> parentEntityIris = Set.of(parentEntityIri1, parentEntityIri2);
        ProjectId projectId = ProjectId.generate();
        UserId userId = UserId.valueOf(getRandomString());
        var executionContext = new ExecutionContext(userId, "jwt", UUID.randomUUID().toString());

        LinearizationSpecification currSpec1 = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.TRUE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/mms"),
                "");
        LinearizationSpecification currSpec2 = new LinearizationSpecification(
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/pcl"),
                "");
        WhoficEntityLinearizationSpecification currentSpec = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec1, currSpec2));

        Set<LinearizationEvent> linearizationEvents = eventMapper.mapInitialLinearizationSpecificationsToEvents(currentSpec);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(currentSpec));

        var currEntityRevision = LinearizationRevision.create(userId, linearizationEvents);
        var currEntityHistory = EntityLinearizationHistory.create(currenteEtityIri.toString(), projectId.id(), Set.of(currEntityRevision));

        mongoTemplate.save(currEntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        LinearizationSpecification missingSpec1 = new LinearizationSpecification(
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/icd-o"),
                "");
        WhoficEntityLinearizationSpecification parentWhoficSpec1 = new WhoficEntityLinearizationSpecification(parentEntityIri1, null, List.of(missingSpec1));

        Set<LinearizationEvent> parent1Events = eventMapper.mapInitialLinearizationSpecificationsToEvents(parentWhoficSpec1);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec1));

        var parent1EntityRevision = LinearizationRevision.create(userId, parent1Events);
        var parent1EntityHistory = EntityLinearizationHistory.create(parentEntityIri1.toString(), projectId.id(), Set.of(parent1EntityRevision));

        mongoTemplate.save(parent1EntityHistory, LINEARIZATION_HISTORY_COLLECTION);


        LinearizationSpecification missingSpec2 = new LinearizationSpecification(
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/icd-o"),
                "");

        WhoficEntityLinearizationSpecification parentWhoficSpec2 = new WhoficEntityLinearizationSpecification(parentEntityIri2, null, List.of(missingSpec2));

        Set<LinearizationEvent> parent2Events = eventMapper.mapInitialLinearizationSpecificationsToEvents(parentWhoficSpec2);
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
        var executionContext = new ExecutionContext(userId, "jwt", UUID.randomUUID().toString());

        LinearizationSpecification currSpec1 = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.TRUE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/mms"),
                "");
        LinearizationSpecification currSpec2 = new LinearizationSpecification(
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/mms"),
                "");
        WhoficEntityLinearizationSpecification currentSpec = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec1, currSpec2));

        Set<LinearizationEvent> linearizationEvents = eventMapper.mapInitialLinearizationSpecificationsToEvents(currentSpec);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(currentSpec));

        var currEntityRevision = LinearizationRevision.create(userId, linearizationEvents);
        var currEntityHistory = EntityLinearizationHistory.create(currenteEtityIri.toString(), projectId.id(), Set.of(currEntityRevision));

        mongoTemplate.save(currEntityHistory, LINEARIZATION_HISTORY_COLLECTION);


        WhoficEntityLinearizationSpecification parentWhoficSpec1 = new WhoficEntityLinearizationSpecification(parentEntityIri1, null, List.of(currSpec1));

        Set<LinearizationEvent> parent1Events = eventMapper.mapInitialLinearizationSpecificationsToEvents(parentWhoficSpec1);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec1));

        var parent1EntityRevision = LinearizationRevision.create(userId, parent1Events);
        var parent1EntityHistory = EntityLinearizationHistory.create(parentEntityIri1.toString(), projectId.id(), Set.of(parent1EntityRevision));

        mongoTemplate.save(parent1EntityHistory, LINEARIZATION_HISTORY_COLLECTION);

        WhoficEntityLinearizationSpecification parentWhoficSpec2 = new WhoficEntityLinearizationSpecification(parentEntityIri2, null, List.of(currSpec2));

        Set<LinearizationEvent> parent2Events = eventMapper.mapInitialLinearizationSpecificationsToEvents(parentWhoficSpec2);
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