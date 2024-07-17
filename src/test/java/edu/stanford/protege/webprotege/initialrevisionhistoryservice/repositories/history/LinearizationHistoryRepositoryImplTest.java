package edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationRevisionHelper.getLinearizationRevision;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
public class LinearizationHistoryRepositoryImplTest extends IntegrationTest {

    @Autowired
    private LinearizationHistoryRepository linearizationHistoryRepository;

    @Test
    public void GIVEN_newLinearizationHistory_WHEN_historyIsSaved_THEN_weGetBackSavedHistory() {
        var projectId = ProjectId.generate();
        var newHistory = getEntityLinearizationHistory(projectId, 2);
        var savedHistory = linearizationHistoryRepository.saveLinearizationHistory(newHistory);
        assertEquals(newHistory, savedHistory);


        var fetchedHistory = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(newHistory.getWhoficEntityIri(), projectId);

        assertEquals(newHistory, fetchedHistory);
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