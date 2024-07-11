package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
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


        lastRevision.linearizationEvents()
                .forEach(event -> {
                    if (event instanceof SetIncludedInLinearization includedInLinearizationEvent) {
                        var eventFoundInResponse = response.linearizationSpecifications()
                                .stream()
                                .filter(spec -> spec.getLinearizationView().equals(includedInLinearizationEvent.getLinearizationView()))
                                .anyMatch(spec -> {
                                    return includedInLinearizationEvent.getValue().equals(spec.getIsIncludedInLinearization().name());
                                });
                        assertTrue("SetIncludedInLinearization not found in response even though we have it in latest revision",
                                eventFoundInResponse);
                    } else if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent) {
                        var eventFoundInResponse = response.linearizationSpecifications()
                                .stream()
                                .filter(spec -> spec.getLinearizationView().equals(auxiliaryAxisChildEvent.getLinearizationView()))
                                .anyMatch(spec -> auxiliaryAxisChildEvent.getValue().equals(spec.getIsAuxiliaryAxisChild().name()));
                        assertTrue("SetAuxiliaryAxisChild not found in response even though we have it in latest revision",
                                eventFoundInResponse);
                    } else if (event instanceof SetLinearizationParent linearizationParentEvent) {
                        var eventFoundInResponse = response.linearizationSpecifications()
                                .stream()
                                .filter(spec -> spec.getLinearizationView().equals(linearizationParentEvent.getLinearizationView()))
                                .anyMatch(spec -> linearizationParentEvent.getValue().equals(spec.getLinearizationParent().toString()));
                        assertTrue("SetLinearizationParent not found in response even though we have it in latest revision",
                                eventFoundInResponse);
                    } else if (event instanceof SetGrouping setGroupingEvent) {
                        var eventFoundInResponse = response.linearizationSpecifications()
                                .stream()
                                .filter(spec -> spec.getLinearizationView().equals(setGroupingEvent.getLinearizationView()))
                                .anyMatch(spec -> {
                                    return setGroupingEvent.getValue().equals(spec.getIsGrouping().name());
                                });
                        assertTrue("SetGrouping not found in response even though we have it in latest revision",
                                eventFoundInResponse);
                    } else if (event instanceof SetCodingNote setCodingNoteEvent) {
                        var eventFoundInResponse = response.linearizationSpecifications()
                                .stream()
                                .filter(spec -> spec.getLinearizationView().equals(setCodingNoteEvent.getLinearizationView()))
                                .anyMatch(spec -> setCodingNoteEvent.getValue().equals(spec.getCodingNote()));
                        assertTrue("SetCodingNote not found in response even though we have it in latest revision",
                                eventFoundInResponse);
                    }
                });
    }
}