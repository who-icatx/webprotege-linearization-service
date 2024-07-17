package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.services.LinearizationEventsProcessorService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.MessageFormat;
import java.util.logging.Logger;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LinearizationEventsProcessorServiceTest {

    public String eventInLatestRevisionNotFoundInResult = "{0} change not found in response even though we have it in latest revision";

    private static final Logger logger = Logger.getLogger(LinearizationEventsProcessorServiceTest.class.getName());

    private LinearizationEventsProcessorService eventsProcessorService;

    @Before
    public void setUp() {
        eventsProcessorService = new LinearizationEventsProcessorService();
    }

    @Test
    public void GIVEN_aLinearizationHistory_WHEN_processingTheHistory_THEN_resultContainsLatestChanges() {
        var projectId = ProjectId.generate();
        //revisions must be sorted chronologically by timestamp
        var linearizationHistory = getEntityLinearizationHistory(projectId, 33);
        var revisions = linearizationHistory.getLinearizationRevisions();
        var lastRevision = revisions.stream().toList().get(revisions.size() - 1);
        WhoficEntityLinearizationSpecification response = eventsProcessorService.processHistory(linearizationHistory);


        //This takes the last revision and check that the changes for it are present in the final response
        lastRevision.linearizationEvents()
                .forEach(event -> {
                            if (event instanceof SetIncludedInLinearization includedInLinearizationEvent) {

                                var eventFoundInResponse = response.linearizationSpecifications()
                                        .stream()
                                        .filter(spec -> spec.getLinearizationView().toString().equals(includedInLinearizationEvent.getLinearizationView()))
                                        .anyMatch(spec -> includedInLinearizationEvent.getValue().equals(spec.getIsIncludedInLinearization().name()));

                                assertTrue(MessageFormat.format(eventInLatestRevisionNotFoundInResult, includedInLinearizationEvent.getType()),
                                        eventFoundInResponse);
                            } else if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent) {

                                var eventFoundInResponse = response.linearizationSpecifications()
                                        .stream()
                                        .filter(spec -> spec.getLinearizationView().toString().equals(auxiliaryAxisChildEvent.getLinearizationView()))
                                        .anyMatch(spec -> auxiliaryAxisChildEvent.getValue().equals(spec.getIsAuxiliaryAxisChild().name()));

                                assertTrue(MessageFormat.format(eventInLatestRevisionNotFoundInResult, auxiliaryAxisChildEvent.getType()),
                                        eventFoundInResponse);
                            } else if (event instanceof SetLinearizationParent linearizationParentEvent) {

                                var eventFoundInResponse = response.linearizationSpecifications()
                                        .stream()
                                        .filter(spec -> spec.getLinearizationView().toString().equals(linearizationParentEvent.getLinearizationView()))
                                        .anyMatch(spec -> linearizationParentEvent.getValue().equals(spec.getLinearizationParent().toString()));

                                assertTrue(MessageFormat.format(eventInLatestRevisionNotFoundInResult, linearizationParentEvent.getType()),
                                        eventFoundInResponse);
                            } else if (event instanceof SetGrouping setGroupingEvent) {

                                var eventFoundInResponse = response.linearizationSpecifications()
                                        .stream()
                                        .filter(spec -> spec.getLinearizationView().toString().equals(setGroupingEvent.getLinearizationView()))
                                        .anyMatch(spec -> setGroupingEvent.getValue().equals(spec.getIsGrouping().name()));

                                assertTrue(MessageFormat.format(eventInLatestRevisionNotFoundInResult, setGroupingEvent.getType()),
                                        eventFoundInResponse);
                            } else if (event instanceof SetCodingNote setCodingNoteEvent) {

                                var eventFoundInResponse = response.linearizationSpecifications()
                                        .stream()
                                        .filter(spec -> spec.getLinearizationView().toString().equals(setCodingNoteEvent.getLinearizationView()))
                                        .anyMatch(spec -> setCodingNoteEvent.getValue().equals(spec.getCodingNote()));

                                assertTrue(MessageFormat.format(eventInLatestRevisionNotFoundInResult, setCodingNoteEvent.getType()),
                                        eventFoundInResponse);
                            }
                        }
                );
    }
}