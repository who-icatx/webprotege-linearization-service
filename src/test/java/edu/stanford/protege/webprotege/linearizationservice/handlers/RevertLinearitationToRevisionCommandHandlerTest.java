package edu.stanford.protege.webprotege.linearizationservice.handlers;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.services.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;
import reactor.core.publisher.Mono;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.LinearizationEventHelper.getRandomLinearizationEvents;
import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RevertLinearitationToRevisionCommandHandlerTest {

    @Mock
    private LinearizationHistoryService historyService;

    @Mock
    private LinearizationEventsProcessorService eventsProcessorService;

    @InjectMocks
    private RevertLinearitationToRevisionCommandHandler commandHandler;

    private RevertLinearitationToRevisionRequest request;
    private ExecutionContext executionContext;
    private UserId userId;
    private ProjectId projectId;
    private IRI entityIri;

    @BeforeEach
    void setUp() {
        userId = UserId.valueOf(getRandomString());
        projectId = ProjectId.generate();
        entityIri = IRI.create(getRandomIri());
        request = new RevertLinearitationToRevisionRequest(entityIri, 1620000000000L, projectId);
        executionContext = new ExecutionContext(userId, "");
    }

    @Test
    void GIVEN_existingHistory_WHEN_handleRequest_THEN_revertsToRevision() {
        // Arrange
        Set<LinearizationRevision> revisions = Set.of(
                LinearizationRevision.create(userId, getRandomLinearizationEvents()),
                LinearizationRevision.create(userId, getRandomLinearizationEvents())
        );
        EntityLinearizationHistory history = EntityLinearizationHistory.create(entityIri.toString(), projectId.id(), revisions);
        when(historyService.getExistingHistoryOrderedByRevision(entityIri, projectId)).thenReturn(Optional.of(history));

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(entityIri, null, List.of());
        when(eventsProcessorService.processHistory(any(), eq(history.getWhoficEntityIri()))).thenReturn(newSpec);

        Mono<RevertLinearitationToRevisionResponse> response = commandHandler.handleRequest(request, executionContext);

        assertNotNull(response.block());
        verify(historyService).addRevision(newSpec, projectId, userId);
    }

    @Test
    void GIVEN_noExistingHistory_WHEN_handleRequest_THEN_noRevisionAdded() {
        when(historyService.getExistingHistoryOrderedByRevision(entityIri, projectId)).thenReturn(Optional.empty());

        Mono<RevertLinearitationToRevisionResponse> response = commandHandler.handleRequest(request, executionContext);

        assertNotNull(response.block());
        verify(historyService, never()).addRevision(any(), any(), any());
    }
}
