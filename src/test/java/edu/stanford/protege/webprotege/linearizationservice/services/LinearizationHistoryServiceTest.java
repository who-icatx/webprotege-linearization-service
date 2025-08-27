package edu.stanford.protege.webprotege.linearizationservice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.mappers.LinearizationEventMapper;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.repositories.history.LinearizationHistoryRepository;
import edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationViewIriHelper;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LinearizationHistoryServiceTest {

    @Mock
    private LinearizationHistoryRepository linearizationHistoryRepo;

    private ObjectMapper objectMapper;

    private LinearizationEventMapper eventMapper;

    private LinearizationEventsProcessorService processorService;

    @Mock
    private ReadWriteLockService readWriteLock;

    @Mock
    private LinearizationDefinitionService definitionService;
    @Mock
    private NewRevisionsEventEmitterServiceImpl newRevisionsEventEmitter;
    @Mock
    private LinearizationDefinitionRepository linearizationDefinitionRepository;
    @Spy
    private LinearizationHistoryService linearizationHistoryService;

    @Before
    public void setUp() throws ExecutionException, InterruptedException, IOException {
        MockitoAnnotations.openMocks(this);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(readWriteLock).executeWriteLock(any(Runnable.class));
        when(definitionService.getUserAccessibleLinearizations(any(), any(), any(), any()))
                .thenReturn(new LinearizationDefinitionService.AllowedLinearizationDefinitions(LinearizationViewIriHelper.getLinearizationViewIris()
                        .stream().map(IRI::toString).collect(Collectors.toList()), new ArrayList<>()));
        objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/LinearizationDefinitions.json");
        when(linearizationDefinitionRepository.getLinearizationDefinitions())
                .thenReturn(objectMapper.readValue(fileInputStream, new TypeReference<>() {
                }));
        eventMapper = new LinearizationEventMapper(linearizationDefinitionRepository);
        processorService = new LinearizationEventsProcessorServiceImpl(definitionService);
        linearizationHistoryService = spy(new LinearizationHistoryServiceImpl(objectMapper, linearizationHistoryRepo, eventMapper, readWriteLock, processorService, newRevisionsEventEmitter));
    }

    @Test
    public void GIVEN_linearizationRevisionsListUnordered_WHEN_requestedFromRepo_THEN_listWillBeOrderedByTimestamp() {
        String entityIri = getRandomIri();
        UserId userid = UserId.valueOf("user1");
        ProjectId projectId = ProjectId.generate();

        // Create unsorted LinearizationRevisions
        LinearizationRevision revision1 = new LinearizationRevision(3L, userid, new HashSet<>(), null, CommitStatus.COMMITTED);
        LinearizationRevision revision2 = new LinearizationRevision(1L, userid, new HashSet<>(), null, CommitStatus.COMMITTED);
        LinearizationRevision revision3 = new LinearizationRevision(2L, userid, new HashSet<>(), null, CommitStatus.COMMITTED);

        Set<LinearizationRevision> unsortedRevisions = new HashSet<>(Arrays.asList(revision1, revision2, revision3));

        var unsortedHistory = Optional.of(new EntityLinearizationHistory(entityIri, projectId.id(), unsortedRevisions));

        // Mock the repository to return the unsorted history
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(entityIri, projectId)).thenReturn(unsortedHistory);

        // Call the method to be tested
        var sortedHistoryOptional = linearizationHistoryService.getExistingHistoryOrderedByRevision(IRI.create(entityIri), projectId);

        assertTrue(sortedHistoryOptional.isPresent());
        // Verify the revisions are sorted by timestamp
        List<LinearizationRevision> sortedRevisions = new ArrayList<>(sortedHistoryOptional.get().getLinearizationRevisions());
        assertEquals(3, sortedRevisions.size());
        assertEquals(1L, sortedRevisions.get(0).timestamp());
        assertEquals(2L, sortedRevisions.get(1).timestamp());
        assertEquals(3L, sortedRevisions.get(2).timestamp());
    }

    @Test
    public void GIVEN_entityWithNoLinearizationHistory_WHEN_savingNewRevision_THEN_newHistoryWithRevisionIsCreated() {
        var userId = UserId.valueOf("user1");
        var linearizationView = "http://id.who.int/icd/release/11/mms";
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        var residual = new LinearizationResiduals(LinearizationStateCell.FALSE, LinearizationStateCell.FALSE,getRandomString(), getRandomString());

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(any(), any()))
                .thenReturn(Optional.empty());
        linearizationHistoryService.addRevision(woficEntitySpec, new ExecutionContext(), projectId, userId);

        verify(linearizationHistoryRepo).saveLinearizationHistory(any());

        verify(linearizationHistoryRepo, times(0))
                .addRevision(any(), any(), any());
    }

    @Test
    public void GIVEN_entityWithALinearizationHistory_WHEN_savingNewRevision_THEN_addnewRevisionToHistory() {
        var userId = UserId.valueOf("user1");
        var linearizationView = "http://id.who.int/icd/release/11/mms";
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );
        var residual = new LinearizationResiduals(LinearizationStateCell.FALSE, LinearizationStateCell.FALSE,getRandomString(),  getRandomString());
        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );
        var existingHistory = getEntityLinearizationHistory(projectId, 2);
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(any(), any()))
                .thenReturn(Optional.of(existingHistory));
        linearizationHistoryService.addRevision(woficEntitySpec,new ExecutionContext(), projectId, userId);

        verify(linearizationHistoryRepo, times(0))
                .saveLinearizationHistory(any());
        verify(newRevisionsEventEmitter, times(0))
                .emitNewRevisionsEvent(eq(projectId),any(), any());

        verify(linearizationHistoryRepo).addRevision(any(), any(), any());
        verify(newRevisionsEventEmitter).emitNewRevisionsEvent(eq(projectId),eq(woficEntitySpec.entityIRI().toString()),any(), any(), eq(null));
    }

    @Test
    public void GIVEN_batchOfHistories_WHEN_batchProcessorIsExecuted_THEN_saveMultipleEntityLinearizationHistoriesAndEmitNewRevisionsEventAreCalled() {
        ProjectId projectId = ProjectId.generate();
        UserId userId = UserId.valueOf("user1");
        IRI currenteEtityIri = IRI.create(getRandomIri());
        IRI currenteEtityIri1 = IRI.create(getRandomIri());

        LinearizationSpecification currSpec11 = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.TRUE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/mms"),
                "");
        LinearizationSpecification currSpec21 = new LinearizationSpecification(
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/pcl"),
                "");
        WhoficEntityLinearizationSpecification currentSpec1 = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec11, currSpec21));

        LinearizationSpecification currSpec12 = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.TRUE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/mms"),
                "");
        LinearizationSpecification currSpec22 = new LinearizationSpecification(
                LinearizationStateCell.UNKNOWN,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE,
                IRI.create(""),
                IRI.create("http://id.who.int/icd/release/11/pcl"),
                "");
        WhoficEntityLinearizationSpecification currentSpec2 = new WhoficEntityLinearizationSpecification(currenteEtityIri1, null, List.of(currSpec12, currSpec22));
        Set<WhoficEntityLinearizationSpecification> page = Set.of(currentSpec1, currentSpec2);

        Consumer<List<WhoficEntityLinearizationSpecification>> batchProcessor =
                linearizationHistoryService.createBatchProcessorForSavingPaginatedHistories(projectId, userId);

        batchProcessor.accept(List.copyOf(page));

        verify(linearizationHistoryService, times(1))
                .saveMultipleEntityLinearizationHistories(anySet());
    }

}