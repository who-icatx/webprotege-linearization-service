package edu.stanford.protege.webprotege.linearizationservice.repositories.history;

import com.mongodb.client.MongoClient;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.*;
import edu.stanford.protege.webprotege.linearizationservice.model.EntityLinearizationHistory;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.LinearizationRevisionWithEntity;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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
    void GIVEN_validProjectIdAndFirstPage_WHEN_getOrderedAndPagedHistoriesForProjectId_THEN_returnFirstPageResultsSortedByTimestampDesc() {
        ProjectId projectId = new ProjectId(testProjectId);
        int pageSize = 1;
        int pageNumber = 1;

        linearizationHistoryRepository.saveLinearizationHistory(getEntityLinearizationHistory(whoficEntityIri1, projectId, 0));
        linearizationHistoryRepository.saveLinearizationHistory(getEntityLinearizationHistory(whoficEntityIri2, projectId, 0));

        var revision1 = getLinearizationRevision();
        var revision2 = getLinearizationRevision();
        var revision3 = getLinearizationRevision();
        var revision4 = getLinearizationRevision();
        var revision5 = getLinearizationRevision();
        var revision6 = getLinearizationRevision();
        var revision7 = getLinearizationRevision();

        linearizationHistoryRepository.addRevision(whoficEntityIri2, projectId, revision1);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision2);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision3);
        linearizationHistoryRepository.addRevision(whoficEntityIri2, projectId, revision4);
        linearizationHistoryRepository.addRevision(whoficEntityIri2, projectId, revision5);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision6);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision7);

        List<LinearizationRevisionWithEntity> results = linearizationHistoryRepository.getOrderedAndPagedHistoriesForProjectId(projectId, pageSize, pageNumber);

        assertEquals(1, results.size());
        assertEquals(whoficEntityIri1, results.get(0).getWhoficEntityIri());

        assertEquals(revision7.timestamp(), results.get(0).getRevision().timestamp());
    }


    @Test
    void GIVEN_validProjectIdAndSecondPage_WHEN_getOrderedAndPagedHistoriesForProjectId_THEN_returnSecondPageResultsSortedByTimestampDesc() {
        ProjectId projectId = new ProjectId(testProjectId);
        int pageSize = 1;
        int pageNumber = 2;

        linearizationHistoryRepository.saveLinearizationHistory(getEntityLinearizationHistory(whoficEntityIri1, projectId, 0));
        linearizationHistoryRepository.saveLinearizationHistory(getEntityLinearizationHistory(whoficEntityIri2, projectId, 0));

        var revision1 = getLinearizationRevision();
        var revision2 = getLinearizationRevision();
        var revision3 = getLinearizationRevision();
        var revision4 = getLinearizationRevision();
        var revision5 = getLinearizationRevision();
        var revision6 = getLinearizationRevision();
        var revision7 = getLinearizationRevision();

        linearizationHistoryRepository.addRevision(whoficEntityIri2, projectId, revision1);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision2);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision3);
        linearizationHistoryRepository.addRevision(whoficEntityIri2, projectId, revision4);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision5);
        linearizationHistoryRepository.addRevision(whoficEntityIri2, projectId, revision6);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision7);

        List<LinearizationRevisionWithEntity> results = linearizationHistoryRepository.getOrderedAndPagedHistoriesForProjectId(projectId, pageSize, pageNumber);

        assertEquals(1, results.size());
        assertEquals(whoficEntityIri2, results.get(0).getWhoficEntityIri());

        assertEquals(revision6.timestamp(), results.get(0).getRevision().timestamp());
    }


    @Test
    void GIVEN_validProjectIdAndOutOfBoundsPage_WHEN_getOrderedAndPagedHistoriesForProjectId_THEN_returnEmptyResults() {
        ProjectId projectId = new ProjectId(testProjectId);
        int pageSize = 1;
        int pageNumber = 3;

        linearizationHistoryRepository.saveLinearizationHistory(getEntityLinearizationHistory(whoficEntityIri1, projectId, 0));
        linearizationHistoryRepository.saveLinearizationHistory(getEntityLinearizationHistory(whoficEntityIri2, projectId, 0));

        var revision1 = getLinearizationRevision();
        var revision2 = getLinearizationRevision();

        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision1);
        linearizationHistoryRepository.addRevision(whoficEntityIri1, projectId, revision2);

        List<LinearizationRevisionWithEntity> results = linearizationHistoryRepository.getOrderedAndPagedHistoriesForProjectId(projectId, pageSize, pageNumber);

        assertEquals(0, results.size());
    }


}