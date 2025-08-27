package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import edu.stanford.protege.webprotege.criteria.MultiMatchType;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.*;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationViewIriHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory.*;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistoryForEntityIri;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.getRandomIri;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


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

    @MockBean
    private CommandExecutor<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> getAuthorizedActionsExecutor;

    @MockBean
    private LinearizationDefinitionRepository definitionRepository;

    @MockBean
    private CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor;

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
    void GIVEN_parentWithLinearizationHistory_WHEN_creatingChildOfParent_THEN_alsoCreatInitialRevisionUsingParentHistoryWithDefaultValues() {


        var userId = UserId.valueOf("user1");
        var parentIri = getRandomIri();
        var newEntityIri = getRandomIri();
        var projectId = ProjectId.generate();
        var executionContext = new ExecutionContext(userId, "jwt", UUID.randomUUID().toString() );

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
             if (event instanceof SetCodingNote) {
                assertEquals("", event.getValue());
            } else if (event instanceof SetIncludedInLinearization) {
                assertEquals(LinearizationStateCell.UNKNOWN.name(), event.getValue());
            } else if (event instanceof SetLinearizationParent) {
                assertEquals("", event.getValue());
            } else if (event instanceof SetSuppressedOtherSpecifiedResidual) {
                assertEquals(LinearizationStateCell.UNKNOWN.name(), event.getValue());
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
                            } else if (parentEvent instanceof SetSuppressedOtherSpecifiedResidual) {
                                var residualExistsInChild = newEntityRevision.linearizationEvents()
                                        .stream()
                                        .anyMatch(childEntityEvent -> (childEntityEvent instanceof SetSuppressedOtherSpecifiedResidual));

                                assertTrue(residualExistsInChild);
                            }
                        }
                );
    }
}