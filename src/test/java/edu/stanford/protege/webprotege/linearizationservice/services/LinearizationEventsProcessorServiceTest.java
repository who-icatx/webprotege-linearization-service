package edu.stanford.protege.webprotege.linearizationservice.services;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRowsCapability;
import edu.stanford.protege.webprotege.linearizationservice.model.WhoficEntityLinearizationSpecification;
import edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationViewIriHelper;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinearizationEventsProcessorServiceTest {

    public String eventInLatestRevisionNotFoundInResult = "{0} change not found in response even though we have it in latest revision";

    private static final Logger logger = Logger.getLogger(LinearizationEventsProcessorServiceTest.class.getName());


    @Mock
    private LinearizationDefinitionService definitionService;
    @InjectMocks
    private LinearizationEventsProcessorServiceImpl eventsProcessorService;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        MockitoAnnotations.openMocks(this);
        when(definitionService.getUserAccessibleLinearizations(any(), any(), any()))
                .thenReturn(new LinearizationDefinitionService.AllowedLinearizationDefinitions(LinearizationViewIriHelper.getLinearizationViewIris()
                        .stream().map(IRI::toString).collect(Collectors.toList()), new ArrayList<>()));
    }

    @Test
    public void GIVEN_aLinearizationHistory_WHEN_processingTheHistory_THEN_resultContainsLatestChanges() {
        var projectId = ProjectId.generate();

        //revisions must be sorted chronologically by timestamp
        var linearizationHistory = getEntityLinearizationHistory(projectId, 33);
        var revisions = linearizationHistory.getLinearizationRevisions();
        var lastRevision = revisions.stream().toList().get(revisions.size() - 1);
        WhoficEntityLinearizationSpecification response = eventsProcessorService.processHistory(linearizationHistory, new ExecutionContext());


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