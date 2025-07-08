package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationEventsProcessorService;
import edu.stanford.protege.webprotege.linearizationservice.services.LinearizationHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetParentsThatAreLinearizationPathParentsCommandHandlerTest {

    @Mock
    private LinearizationHistoryService linearizationHistoryService;

    @Mock
    private LinearizationEventsProcessorService linearizationEventsProcessor;

    private GetParentsThatAreLinearizationPathParentsCommandHandler commandHandler;

    private ProjectId projectId;
    private ExecutionContext executionContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commandHandler = new GetParentsThatAreLinearizationPathParentsCommandHandler(linearizationHistoryService, linearizationEventsProcessor);
        projectId = ProjectId.generate();
        executionContext = mock(ExecutionContext.class);
    }

    @Test
    void GIVEN_validRequest_WHEN_handleRequest_THEN_returnsMatchingParents() {
        IRI entityIri = IRI.create("http://example.org/entity");
        IRI parentIri1 = IRI.create("http://example.org/parent1");
        IRI parentIri2 = IRI.create("http://example.org/parent2");
        IRI parentIri3 = IRI.create("http://example.org/parent3");
        IRI linearizationView1 = IRI.create("http://example.org/linearizationView1");
        IRI linearizationView2 = IRI.create("http://example.org/linearizationView2");

        LinearizationRevision linearizationRevision = mock(LinearizationRevision.class);

        EntityLinearizationHistory entityHistory = new EntityLinearizationHistory(
                entityIri.toString(),
                projectId.id(),
                Set.of(linearizationRevision)
        );

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(entityIri, projectId))
                .thenReturn(Optional.of(entityHistory));

        List<LinearizationSpecification> linearizationSpecifications = List.of(
                new LinearizationSpecification(
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        parentIri1,
                        linearizationView1,
                        ""
                ),
                new LinearizationSpecification(
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        parentIri2,
                        linearizationView2,
                        ""
                )
        );

        WhoficEntityLinearizationSpecification processedHistory = new WhoficEntityLinearizationSpecification(
                entityIri,
                null,
                linearizationSpecifications
        );

        when(linearizationEventsProcessor.processHistory(any(), any())).thenReturn(processedHistory);

        GetParentsThatAreLinearizationPathParentsRequest request =
                new GetParentsThatAreLinearizationPathParentsRequest(entityIri, Set.of(parentIri1, parentIri2, parentIri3), projectId);

        Mono<GetParentsThatAreLinearizationPathParentsResponse> responseMono = commandHandler.handleRequest(request, executionContext);

        GetParentsThatAreLinearizationPathParentsResponse response = responseMono.block();
        assertNotNull(response);
        assertEquals(Set.of(parentIri1, parentIri2), response.parentsThatAreLinearizationPathParents());

        verify(linearizationHistoryService).getExistingHistoryOrderedByRevision(entityIri, projectId);
        verify(linearizationEventsProcessor).processHistory(any(), any());
    }


    @Test
    void GIVEN_noMatchingParents_WHEN_handleRequest_THEN_returnsEmptySet() {
        IRI currentEntityIri = IRI.create("http://example.org/currentEntity");

        IRI requestedParentIri1 = IRI.create("http://example.org/requestedParent1");
        IRI requestedParentIri2 = IRI.create("http://example.org/requestedParent2");
        Set<IRI> parentEntityIris = Set.of(requestedParentIri1, requestedParentIri2);

        IRI historyParentIri1 = IRI.create("http://example.org/historyParent1");
        IRI historyParentIri2 = IRI.create("http://example.org/historyParent2");
        IRI linearizationView1 = IRI.create("http://example.org/linearizationView1");
        IRI linearizationView2 = IRI.create("http://example.org/linearizationView2");

        List<LinearizationSpecification> linearizationSpecifications = List.of(
                new LinearizationSpecification(
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        historyParentIri1,
                        linearizationView1,
                        ""
                ),
                new LinearizationSpecification(
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        ThreeStateBoolean.UNKNOWN,
                        historyParentIri2,
                        linearizationView2,
                        ""
                )
        );

        EntityLinearizationHistory entityHistory = new EntityLinearizationHistory(
                currentEntityIri.toString(),
                projectId.id(),
                Set.of(mock(LinearizationRevision.class))
        );

        when(linearizationHistoryService.getExistingHistoryOrderedByRevision(currentEntityIri, projectId))
                .thenReturn(Optional.of(entityHistory));

        WhoficEntityLinearizationSpecification processedSpec = new WhoficEntityLinearizationSpecification(
                currentEntityIri, null, linearizationSpecifications
        );

        when(linearizationEventsProcessor.processHistory(any(), any()))
                .thenReturn(processedSpec);

        GetParentsThatAreLinearizationPathParentsRequest request =
                new GetParentsThatAreLinearizationPathParentsRequest(currentEntityIri, parentEntityIris, projectId);

        Mono<GetParentsThatAreLinearizationPathParentsResponse> responseMono = commandHandler.handleRequest(request, executionContext);

        GetParentsThatAreLinearizationPathParentsResponse response = responseMono.block();

        assertNotNull(response);
        assertEquals(Set.of(), response.parentsThatAreLinearizationPathParents());

        verify(linearizationHistoryService).getExistingHistoryOrderedByRevision(currentEntityIri, projectId);
        verify(linearizationEventsProcessor).processHistory(any(), any());
    }


}
