package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
class LinearizationRevisionServiceTest {

    @Mock
    private LinearizationRevisionRepository linearizationRevisionRepo;

    @InjectMocks
    private LinearizationRevisionService linearizationRevisionService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void GIVEN_linearizationRevisionsListUnordered_WHEN_requestedFromRepo_THEN_listWillBeOrderedByTimestamp() {
        String entityIri = "http://example.com/entity";
        ProjectId projectId = ProjectId.generate();

        // Create unsorted LinearizationRevisions
        LinearizationRevision revision1 = new LinearizationRevision(3L, "user1", new HashSet<>());
        LinearizationRevision revision2 = new LinearizationRevision(1L, "user2", new HashSet<>());
        LinearizationRevision revision3 = new LinearizationRevision(2L, "user3", new HashSet<>());

        Set<LinearizationRevision> unsortedRevisions = new HashSet<>(Arrays.asList(revision1, revision2, revision3));

        EntityLinearizationHistory unsortedHistory = new EntityLinearizationHistory("http://example.com/entity", projectId, unsortedRevisions);

        // Mock the repository to return the unsorted history
        when(linearizationRevisionRepo.getExistingHistory(entityIri, projectId)).thenReturn(unsortedHistory);

        // Call the method to be tested
        EntityLinearizationHistory sortedHistory = linearizationRevisionService.getExistingHistoryOrderedByRevision(entityIri, projectId);

        // Verify the revisions are sorted by timestamp
        List<LinearizationRevision> sortedRevisions = new ArrayList<>(sortedHistory.getLinearizationRevisions());
        assertEquals(3, sortedRevisions.size());
        assertEquals(1L, sortedRevisions.get(0).timestamp());
        assertEquals(2L, sortedRevisions.get(1).timestamp());
        assertEquals(3L, sortedRevisions.get(2).timestamp());
    }
}