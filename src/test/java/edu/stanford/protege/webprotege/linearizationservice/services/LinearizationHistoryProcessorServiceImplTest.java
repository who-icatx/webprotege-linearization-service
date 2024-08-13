package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinearizationHistoryProcessorServiceImplTest {

    @Mock
    private LinearizationEventMapper eventMapper;

    @Mock
    private LinearizationHistoryService linearizationHistoryService;

    @Mock
    private LinearizationEventsProcessorService eventsProcessorService;

    @Mock
    private WhoficEntityLinearizationSpecificationMapper whoficSpecMapper;

    @Mock
    private ReadWriteLockService readWriteLockService;

    @InjectMocks
    private LinearizationHistoryProcessorServiceImpl historyProcessorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        doAnswer(invocation -> {
            Callable<?> callable = invocation.getArgument(0);
            return callable.call();
        }).when(readWriteLockService).executeReadLock(any(Callable.class));

        eventMapper = new LinearizationEventMapper();
        whoficSpecMapper = new WhoficEntityLinearizationSpecificationMapper();
        historyProcessorService = new LinearizationHistoryProcessorServiceImpl(linearizationHistoryService, eventsProcessorService, whoficSpecMapper, readWriteLockService);
    }

    @Test
    void GIVEN_noParentEntityHistory_WHEN_mergeLinearizationViewsFromParentsAndGetDefaultSpecIsCalled_THEN_returnEmptyOptional() {
        IRI currenteEtityIri = IRI.create(getRandomIri());
        Set<IRI> parentEntityIris = Set.of(IRI.create(getRandomIri()),IRI.create(getRandomIri()));
        ProjectId projectId = ProjectId.generate();

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(any(), any()))
                .thenReturn(Optional.empty());

        Optional<WhoficEntityLinearizationSpecification> result = historyProcessorService
                .mergeLinearizationViewsFromParentsAndGetDefaultSpec(currenteEtityIri, parentEntityIris, projectId);

        assertTrue(result.isEmpty());

        verify(eventsProcessorService, times(0)).processHistory(any());
        verify(linearizationHistoryService, times(3)).getExistingHistoryOrderedByRevision(any(), any());
    }

    @Test
    void GIVEN_parentEntityHistoryWithMissingViews_WHEN_mergeLinearizationViewsFromParentsAndGetDefaultSpecIsCalled_THEN_returnDefaultSpecWithMissingViews() {
        IRI currenteEtityIri = IRI.create(getRandomIri());
        IRI parentEntityIri1 = IRI.create(getRandomIri());
        IRI parentEntityIri2 = IRI.create(getRandomIri());
        Set<IRI> parentEntityIris = Set.of(parentEntityIri1, parentEntityIri2);
        ProjectId projectId = ProjectId.generate();
        UserId userId = UserId.valueOf(getRandomString());

        LinearizationSpecification currSpec1 = new LinearizationSpecification(
                ThreeStateBoolean.TRUE, ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.TRUE, IRI.create(""), IRI.create("http://id.who.int/icd/entity/MMS"), "");
        LinearizationSpecification currSpec2 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.FALSE, ThreeStateBoolean.FALSE, IRI.create(""), IRI.create("http://id.who.int/icd/entity/primCareLowResSet"), "");
        WhoficEntityLinearizationSpecification currentSpec = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec1, currSpec2));

        Set<LinearizationEvent> linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(currentSpec);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(currentSpec));

        var currEntityRevision = LinearizationRevision.create(userId, linearizationEvents);
        var currEntityHistory = EntityLinearizationHistory.create(currenteEtityIri.toString(), projectId.id(), Set.of(currEntityRevision));


        LinearizationSpecification missingSpec1 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.FALSE, IRI.create(""), IRI.create("http://id.who.int/icd/entity/research"), "");
        WhoficEntityLinearizationSpecification parentWhoficSpec1 = new WhoficEntityLinearizationSpecification(parentEntityIri1, null, List.of(missingSpec1));

        Set<LinearizationEvent> parent1Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec1);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec1));

        var parent1EntityRevision = LinearizationRevision.create(userId, parent1Events);
        var parent1EntityHistory = EntityLinearizationHistory.create(parentEntityIri1.toString(), projectId.id(), Set.of(parent1EntityRevision));

        LinearizationSpecification missingSpec2 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.FALSE, IRI.create(""), IRI.create("http://id.who.int/icd/entity/mentalHealth"), "");

        WhoficEntityLinearizationSpecification parentWhoficSpec2 = new WhoficEntityLinearizationSpecification(parentEntityIri2, null, List.of(missingSpec2));

        Set<LinearizationEvent> parent2Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec2);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec2));

        var parent2EntityRevision = LinearizationRevision.create(userId, parent2Events);
        var parent2EntityHistory = EntityLinearizationHistory.create(parentEntityIri2.toString(), projectId.id(), Set.of(parent2EntityRevision));


        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(currenteEtityIri, projectId))
                .thenReturn(Optional.of(currEntityHistory));

        when(eventsProcessorService.processHistory(currEntityHistory))
                .thenReturn(currentSpec);

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(parentEntityIri1, projectId))
                .thenReturn(Optional.of(parent1EntityHistory));

        when(eventsProcessorService.processHistory(parent1EntityHistory))
                .thenReturn(parentWhoficSpec1);

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(parentEntityIri2, projectId))
                .thenReturn(Optional.of(parent2EntityHistory));

        when(eventsProcessorService.processHistory(parent2EntityHistory))
                .thenReturn(parentWhoficSpec2);

        Optional<WhoficEntityLinearizationSpecification> result = historyProcessorService
                .mergeLinearizationViewsFromParentsAndGetDefaultSpec(currenteEtityIri, parentEntityIris, projectId);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().linearizationSpecifications().size());

        verify(linearizationHistoryService, times(1)).getExistingHistoryOrderedByRevision(currenteEtityIri, projectId);
        verify(eventsProcessorService, times(1)).processHistory(currEntityHistory);

        verify(linearizationHistoryService, times(1)).getExistingHistoryOrderedByRevision(parentEntityIri1, projectId);
        verify(eventsProcessorService, times(1)).processHistory(parent1EntityHistory);

        verify(linearizationHistoryService, times(1)).getExistingHistoryOrderedByRevision(parentEntityIri2, projectId);
        verify(eventsProcessorService, times(1)).processHistory(parent2EntityHistory);

        currentSpec.linearizationSpecifications()
                .stream()
                .flatMap(currSpec -> {
                    var newSpecNotInOldSpec = result.get()
                            .linearizationSpecifications()
                            .stream()
                            .noneMatch(resultSpec -> resultSpec.getLinearizationView().equals(currSpec.getLinearizationView()));
                    return Stream.of(newSpecNotInOldSpec);
                })
                .forEach(Assertions::assertTrue);
    }

    @Test
    void GIVEN_parentEntityHistoryWithSameViews_WHEN_mergeLinearizationViewsFromParentsAndGetDefaultSpecIsCalled_THEN_returnEmptyOptional() {
        IRI currenteEtityIri = IRI.create(getRandomIri());
        IRI parentEntityIri1 = IRI.create(getRandomIri());
        IRI parentEntityIri2 = IRI.create(getRandomIri());
        Set<IRI> parentEntityIris = Set.of(parentEntityIri1, parentEntityIri2);
        ProjectId projectId = ProjectId.generate();
        UserId userId = UserId.valueOf(getRandomString());

        LinearizationSpecification currSpec1 = new LinearizationSpecification(
                ThreeStateBoolean.TRUE, ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.TRUE, IRI.create(""), IRI.create("http://id.who.int/icd/entity/MMS"), "");
        LinearizationSpecification currSpec2 = new LinearizationSpecification(
                ThreeStateBoolean.UNKNOWN, ThreeStateBoolean.FALSE, ThreeStateBoolean.FALSE, IRI.create(""), IRI.create("http://id.who.int/icd/entity/primCareLowResSet"), "");
        WhoficEntityLinearizationSpecification currentSpec = new WhoficEntityLinearizationSpecification(currenteEtityIri, null, List.of(currSpec1, currSpec2));

        Set<LinearizationEvent> linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(currentSpec);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(currentSpec));

        var currEntityRevision = LinearizationRevision.create(userId, linearizationEvents);
        var currEntityHistory = EntityLinearizationHistory.create(currenteEtityIri.toString(), projectId.id(), Set.of(currEntityRevision));


        WhoficEntityLinearizationSpecification parentWhoficSpec1 = new WhoficEntityLinearizationSpecification(parentEntityIri1, null, List.of(currSpec1));

        Set<LinearizationEvent> parent1Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec1);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec1));

        var parent1EntityRevision = LinearizationRevision.create(userId, parent1Events);
        var parent1EntityHistory = EntityLinearizationHistory.create(parentEntityIri1.toString(), projectId.id(), Set.of(parent1EntityRevision));

        WhoficEntityLinearizationSpecification parentWhoficSpec2 = new WhoficEntityLinearizationSpecification(parentEntityIri2, null, List.of(currSpec2));

        Set<LinearizationEvent> parent2Events = eventMapper.mapLinearizationSpecificationsToEvents(parentWhoficSpec2);
        parent1Events.addAll(eventMapper.mapLinearizationResidualsToEvents(parentWhoficSpec2));

        var parent2EntityRevision = LinearizationRevision.create(userId, parent2Events);
        var parent2EntityHistory = EntityLinearizationHistory.create(parentEntityIri2.toString(), projectId.id(), Set.of(parent2EntityRevision));


        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(currenteEtityIri, projectId))
                .thenReturn(Optional.of(currEntityHistory));

        when(eventsProcessorService.processHistory(currEntityHistory))
                .thenReturn(currentSpec);

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(parentEntityIri1, projectId))
                .thenReturn(Optional.of(parent1EntityHistory));

        when(eventsProcessorService.processHistory(parent1EntityHistory))
                .thenReturn(parentWhoficSpec1);

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(parentEntityIri2, projectId))
                .thenReturn(Optional.of(parent2EntityHistory));

        when(eventsProcessorService.processHistory(parent2EntityHistory))
                .thenReturn(parentWhoficSpec2);

        Optional<WhoficEntityLinearizationSpecification> result = historyProcessorService
                .mergeLinearizationViewsFromParentsAndGetDefaultSpec(currenteEtityIri, parentEntityIris, projectId);

        assertTrue(result.isEmpty());

        verify(linearizationHistoryService, times(1)).getExistingHistoryOrderedByRevision(currenteEtityIri, projectId);
        verify(eventsProcessorService, times(1)).processHistory(currEntityHistory);

        verify(linearizationHistoryService, times(1)).getExistingHistoryOrderedByRevision(parentEntityIri1, projectId);
        verify(eventsProcessorService, times(1)).processHistory(parent1EntityHistory);

        verify(linearizationHistoryService, times(1)).getExistingHistoryOrderedByRevision(parentEntityIri2, projectId);
        verify(eventsProcessorService, times(1)).processHistory(parent2EntityHistory);
    }
}
