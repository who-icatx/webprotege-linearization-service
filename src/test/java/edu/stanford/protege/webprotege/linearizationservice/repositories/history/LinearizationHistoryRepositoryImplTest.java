package edu.stanford.protege.webprotege.linearizationservice.repositories.history;

import com.mongodb.client.MongoClient;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.*;
import edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationRevisionHelper.getLinearizationRevision;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes = WebprotegeLinearizationServiceServiceApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ExtendWith({SpringExtension.class, IntegrationTest.class})
@ActiveProfiles("test")
public class LinearizationHistoryRepositoryImplTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private LinearizationHistoryRepository linearizationHistoryRepository;

    private final String whoficEntityIri1 = "whoficEntityIri1";
    private final String whoficEntityIri2 = "whoficEntityIri2";
    private final String testProjectId = "testProjectId";

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(EntityLinearizationHistory.class);
    }

    @Test
    public void GIVEN_newLinearizationHistory_WHEN_historyIsSaved_THEN_weGetBackSavedHistory() {
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        var savedHistory = linearizationHistoryRepository.saveLinearizationHistory(newHistory);
        assertEquals(newHistory, savedHistory);


        var fetchedHistory = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(newHistory.getWhoficEntityIri(), projectId);
        assertTrue(fetchedHistory.isPresent());
        assertEquals(newHistory, fetchedHistory.get());
    }

    @Test
    public void GIVEN_existingHistory_WHEN_addingNewRevision_THEN_weGetBackHistoryWIthNewRevision() {
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        var savedHistory = linearizationHistoryRepository.saveLinearizationHistory(newHistory);
        assertEquals(newHistory, savedHistory);


        var initialHistory = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(newHistory.getWhoficEntityIri(), projectId);

        var newRevision = getLinearizationRevision();

        linearizationHistoryRepository.addRevision(newHistory.getWhoficEntityIri(), projectId, newRevision);

        var updatedHistory = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(newHistory.getWhoficEntityIri(), projectId);

        assertNotEquals(initialHistory, updatedHistory);
    }

    @Test
    public void GIVEN_multipleHistories_WHEN_fetchedInBatches_THEN_correctHistoriesAreReturned() {
        ProjectId projectId = ProjectId.generate();
        List<String> iris = new ArrayList<>();
        List<EntityLinearizationHistory> expectedHistories = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String iri = "whoficEntityIri" + i;
            iris.add(iri);
            EntityLinearizationHistory history = getEntityLinearizationHistory(projectId, 1);
            history = new EntityLinearizationHistory(iri, projectId.value(), history.getLinearizationRevisions());
            linearizationHistoryRepository.saveLinearizationHistory(history);
            expectedHistories.add(history);
        }

        List<EntityLinearizationHistory> fetchedHistories = linearizationHistoryRepository.findHistoriesByEntityIrisAndProjectIdInBatches(iris, projectId, 3);

        assertEquals(expectedHistories.size(), fetchedHistories.size());
        assertTrue(fetchedHistories.containsAll(expectedHistories));
    }

    @Test
    public void GIVEN_someHistoriesWithNoRevisions_WHEN_fetchedInBatches_THEN_onlyHistoriesWithRevisionsAreReturned() {
        ProjectId projectId = ProjectId.generate();
        List<String> iris = new ArrayList<>();
        List<EntityLinearizationHistory> expectedHistories = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            String iri = "whoficEntityIri" + i;
            iris.add(iri);
            EntityLinearizationHistory history = getEntityLinearizationHistory(projectId, 1);
            history = new EntityLinearizationHistory(iri, projectId.value(), history.getLinearizationRevisions());
            linearizationHistoryRepository.saveLinearizationHistory(history);
            expectedHistories.add(history);
        }

        for (int i = 5; i < 10; i++) {
            String iri = "whoficEntityIri" + i;
            iris.add(iri);
            EntityLinearizationHistory history = new EntityLinearizationHistory(iri, projectId.value(), new HashSet<>());
            linearizationHistoryRepository.saveLinearizationHistory(history);
        }

        List<EntityLinearizationHistory> fetchedHistories = linearizationHistoryRepository.findHistoriesByEntityIrisAndProjectIdInBatches(iris, projectId, 3);

        assertEquals(expectedHistories.size(), fetchedHistories.size());
        assertTrue(fetchedHistories.containsAll(expectedHistories));
    }

    @Test
    public void GIVEN_noHistories_WHEN_fetchedInBatches_THEN_emptyListIsReturned() {
        ProjectId projectId = ProjectId.generate();
        List<String> iris = List.of("nonExistentIri1", "nonExistentIri2");

        List<EntityLinearizationHistory> fetchedHistories = linearizationHistoryRepository.findHistoriesByEntityIrisAndProjectIdInBatches(iris, projectId, 3);

        assertTrue(fetchedHistories.isEmpty());
    }


}