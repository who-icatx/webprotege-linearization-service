package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.mappers.LinearizationEventMapper;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.LinearizationHistoryRepository;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LinearizationHistoryServiceTest {

    @Mock
    private LinearizationHistoryRepository linearizationHistoryRepo;

    @Mock
    private ObjectMapper objectMapper;

    private LinearizationEventMapper eventMapper;

    private LinearizationEventsProcessorService processorService;

    @Mock
    private ReadWriteLockService readWriteLock;

    private LinearizationHistoryService linearizationHistoryService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(readWriteLock).executeWriteLock(any(Runnable.class));
        eventMapper = new LinearizationEventMapper();
        processorService = new LinearizationEventsProcessorServiceImpl();
        linearizationHistoryService = new LinearizationHistoryServiceImpl(objectMapper, linearizationHistoryRepo, eventMapper, readWriteLock, processorService);
    }

    @Test
    public void GIVEN_linearizationRevisionsListUnordered_WHEN_requestedFromRepo_THEN_listWillBeOrderedByTimestamp() {
        String entityIri = getRandomIri();
        UserId userid = UserId.valueOf("user1");
        ProjectId projectId = ProjectId.generate();

        // Create unsorted LinearizationRevisions
        LinearizationRevision revision1 = new LinearizationRevision(3L, userid, new HashSet<>());
        LinearizationRevision revision2 = new LinearizationRevision(1L, userid, new HashSet<>());
        LinearizationRevision revision3 = new LinearizationRevision(2L, userid, new HashSet<>());

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
        var linearizationView = getRandomIri();
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        LinearizationSpecification spec = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE,ThreeStateBoolean.FALSE,getRandomString(), getRandomString());

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(any(), any())).thenReturn(Optional.empty());
        linearizationHistoryService.addRevision(woficEntitySpec, projectId, userId);

        verify(linearizationHistoryRepo).saveLinearizationHistory(any());
        verify(linearizationHistoryRepo, times(0)).addRevision(any(), any(), any());
    }

    @Test
    public void GIVEN_entityWithALinearizationHistory_WHEN_savingNewRevision_THEN_addnewRevisionToHistory() {
        var userId = UserId.valueOf("user1");
        var linearizationView = getRandomIri();
        var linearizationParent = getRandomIri();
        var codingNote = getRandomString();
        var entityIri = getRandomIri();
        var projectId = ProjectId.generate();
        LinearizationSpecification spec = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );
        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE,ThreeStateBoolean.FALSE,getRandomString(),  getRandomString());
        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );
        var existingHistory = getEntityLinearizationHistory(projectId, 2);
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(any(), any())).thenReturn(Optional.of(existingHistory));
        linearizationHistoryService.addRevision(woficEntitySpec, projectId, userId);

        verify(linearizationHistoryRepo, times(0)).saveLinearizationHistory(any());
        verify(linearizationHistoryRepo, times(1)).addRevision(any(), any(), any());
    }
}