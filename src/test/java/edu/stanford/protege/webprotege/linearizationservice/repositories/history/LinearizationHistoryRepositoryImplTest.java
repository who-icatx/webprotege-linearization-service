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


}