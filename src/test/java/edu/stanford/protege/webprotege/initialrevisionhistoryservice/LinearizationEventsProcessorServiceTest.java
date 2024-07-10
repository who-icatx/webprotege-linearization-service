package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationEventHelper.mapLinearizationSpecificationsToEvents;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@Import({WebprotegeLinearizationServiceServiceApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@RunWith(SpringRunner.class)
class LinearizationEventsProcessorServiceTest {

    @Autowired
    private LinearizationEventsProcessorService eventsProcessorService;

    @Test
    void GIVEN_aLinearizationHistory_WHEN_processingTheHistory_THEN_resultContainsLatestChanges() {
        var projectId = ProjectId.generate();
        //revisions must be sorted chronologically by timestamp
        var linearizationHistory = getEntityLinearizationHistory(projectId, 3);
        var revisions = linearizationHistory.getLinearizationRevisions();
        var lastRevision = revisions.stream().toList().get(revisions.size() - 1);
        WhoficEntityLinearizationSpecification response = eventsProcessorService.processHistory(linearizationHistory);

        var processedSpecEvents = mapLinearizationSpecificationsToEvents(response);

        lastRevision.linearizationEvents().forEach(lastRevisionEvent -> {
            assertTrue(processedSpecEvents.stream().anyMatch(processedEvent -> processedEvent.getValue().equals(lastRevisionEvent.getValue())));
        });
    }
}