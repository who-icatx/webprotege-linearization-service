package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.LinearizationHistoryRepository;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.LinearizationHistoryService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class LinearizationHistoryServiceTest {

    @Mock
    private LinearizationHistoryRepository linearizationHistoryRepo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LinearizationEventMapper eventMapper;

    @InjectMocks
    private LinearizationHistoryService linearizationHistoryService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        linearizationHistoryService = new LinearizationHistoryService(objectMapper, linearizationHistoryRepo, eventMapper);
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

        EntityLinearizationHistory unsortedHistory = new EntityLinearizationHistory(entityIri, projectId.id(), unsortedRevisions);

        // Mock the repository to return the unsorted history
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(entityIri, projectId)).thenReturn(unsortedHistory);

        // Call the method to be tested
        EntityLinearizationHistory sortedHistory = linearizationHistoryService.getExistingHistoryOrderedByRevision(IRI.create(entityIri), projectId);

        // Verify the revisions are sorted by timestamp
        List<LinearizationRevision> sortedRevisions = new ArrayList<>(sortedHistory.getLinearizationRevisions());
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

        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());

        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(any(), any())).thenReturn(null);
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
        var residual = new LinearizationResiduals(ThreeStateBoolean.FALSE, getRandomString());
        var woficEntitySpec = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                residual,
                List.of(spec)
        );
        var existingHistory = getEntityLinearizationHistory(projectId, 2);
        when(linearizationHistoryRepo.findHistoryByEntityIriAndProjectId(any(), any())).thenReturn(existingHistory);
        linearizationHistoryService.addRevision(woficEntitySpec, projectId, userId);

        verify(linearizationHistoryRepo, times(0)).saveLinearizationHistory(any());
        verify(linearizationHistoryRepo, times(1)).addRevision(any(), any(), any());
    }
}