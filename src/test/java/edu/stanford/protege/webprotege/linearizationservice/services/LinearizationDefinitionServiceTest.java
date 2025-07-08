package edu.stanford.protege.webprotege.linearizationservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaRequest;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaResponse;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRowsCapability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class LinearizationDefinitionServiceTest {

    @Mock
    private CommandExecutor<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> getAuthorizedActionsExecutor;

    @Mock
    private CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ExecutionContext executionContext;

    private LinearizationDefinitionService service;

    private ProjectId projectId;
    private IRI entityIri;
    private UserId userId;

    private final static List<String> allowedIds = Arrays.asList(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW);

    @BeforeEach
    void setUp() {
        service = new LinearizationDefinitionService(getAuthorizedActionsExecutor, getMatchingCriteriaExecutor, objectMapper);
        projectId = ProjectId.valueOf("00000000-0000-0000-0000-000000000001");
        entityIri = IRI.create("http://test.org/entity");
        userId = UserId.valueOf("test-user");
        when(executionContext.userId()).thenReturn(userId);
    }

    @Test
    void shouldReturnEditableAndReadableLinearizations_whenUserHasBothCapabilities() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability capability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability = mock(GenericParameterizedCapability.class);
        when(capability.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        when(capability.linearizationIds()).thenReturn(List.of(linearizationId));
        when(capability.contextCriteria()).thenReturn(null);
        when(capability.asGenericCapability()).thenReturn(genericCapability);
        when(genericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability));

        // Ensure all keys are present in the response
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability, LinearizationRowsCapability.class))
                .thenReturn(capability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).contains(linearizationId);
        assertThat(result.readableLinearizations()).contains(linearizationId);
    }

    @Test
    void shouldReturnOnlyReadableLinearizations_whenUserHasNoEditCapability() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability capability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability = mock(GenericParameterizedCapability.class);
        when(capability.id()).thenReturn(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW);
        when(capability.linearizationIds()).thenReturn(List.of(linearizationId));
        when(capability.contextCriteria()).thenReturn(null);
        when(capability.asGenericCapability()).thenReturn(genericCapability);
        when(genericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability));

        // Ensure all keys are present in the response
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability, LinearizationRowsCapability.class))
                .thenReturn(capability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).isEmpty();
        assertThat(result.readableLinearizations()).contains(linearizationId);
    }

    @Test
    void shouldReturnEmptyLists_whenUserHasNoCapabilities() throws ExecutionException, InterruptedException {
        // Given
        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of());

        // Return a GetMatchingCriteriaResponse with both keys, but empty list
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).isEmpty();
        assertThat(result.readableLinearizations()).isEmpty();
    }

    @Test
    void shouldHandleMultipleLinearizationIdsPerCapability() throws ExecutionException, InterruptedException {
        // Given
        List<String> linearizationIds = List.of("linearization-1", "linearization-2");
        LinearizationRowsCapability capability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability = mock(GenericParameterizedCapability.class);
        when(capability.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        when(capability.linearizationIds()).thenReturn(linearizationIds);
        when(capability.contextCriteria()).thenReturn(null);
        when(capability.asGenericCapability()).thenReturn(genericCapability);
        when(genericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability));

        // Ensure all keys are present in the response
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability, LinearizationRowsCapability.class))
                .thenReturn(capability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).containsAll(linearizationIds);
        assertThat(result.readableLinearizations()).containsAll(linearizationIds);
    }

    @Test
    void shouldPropagateExecutionException() {
        // Given
        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new ExecutionException("Test exception", null)));

        // When/Then
        assertThatThrownBy(() ->
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext))
                .isInstanceOf(ExecutionException.class)
                .hasMessageContaining("Test exception");
    }

    @Test
    void shouldHandleNullContextCriteria() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability capability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability = mock(GenericParameterizedCapability.class);
        when(capability.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        when(capability.linearizationIds()).thenReturn(List.of(linearizationId));
        when(capability.contextCriteria()).thenReturn(null);
        when(capability.asGenericCapability()).thenReturn(genericCapability);
        when(genericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability));

        // Ensure all keys are present in the response
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability, LinearizationRowsCapability.class))
                .thenReturn(capability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).contains(linearizationId);
        assertThat(result.readableLinearizations()).contains(linearizationId);
    }

    @Test
    void shouldNotAllowEditing_whenContextCriteriaDoesNotMatch() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability capability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability = mock(GenericParameterizedCapability.class);
        CompositeRootCriteria editCriteria = mock(CompositeRootCriteria.class); // Simulate a non-empty context criteria
        lenient().when(capability.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        lenient().when(capability.linearizationIds()).thenReturn(List.of(linearizationId));
        lenient().when(capability.contextCriteria()).thenReturn(editCriteria); // Non-null, non-empty
        lenient().when(capability.asGenericCapability()).thenReturn(genericCapability);
        lenient().when(genericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability));

        // The matching response does NOT include EDIT_LINEARIZATION_ROW, so the context does not match
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW) // Only view capability matches
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability, LinearizationRowsCapability.class))
                .thenReturn(capability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).isEmpty();
        // The user still has the capability, so readable should include it if the logic allows
        assertThat(result.readableLinearizations()).doesNotContain(linearizationId); // Only view is allowed in this context
    }

    @Test
    void shouldNotAllowViewing_whenViewContextCriteriaDoesNotMatch() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability capability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability = mock(GenericParameterizedCapability.class);
        CompositeRootCriteria viewCriteria = mock(CompositeRootCriteria.class);

        lenient().when(capability.id()).thenReturn(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW);
        lenient().when(capability.linearizationIds()).thenReturn(List.of(linearizationId));
        lenient().when(capability.contextCriteria()).thenReturn(viewCriteria);
        lenient().when(capability.asGenericCapability()).thenReturn(genericCapability);
        lenient().when(genericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability));

        // Only EDIT_LINEARIZATION_ROW in matching keys, VIEW_LINEARIZATION_ROW is not present
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability, LinearizationRowsCapability.class))
                .thenReturn(capability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).isEmpty();
        assertThat(result.readableLinearizations()).isEmpty();
    }

    @Test
    void shouldAllowBothEditAndView_whenBothContextCriteriaMatch() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability editCapability = mock(LinearizationRowsCapability.class);
        LinearizationRowsCapability viewCapability = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability editGenericCapability = mock(GenericParameterizedCapability.class);
        GenericParameterizedCapability viewGenericCapability = mock(GenericParameterizedCapability.class);
        CompositeRootCriteria editCriteria = mock(CompositeRootCriteria.class);
        CompositeRootCriteria viewCriteria = mock(CompositeRootCriteria.class);

        // Setup edit capability
        lenient().when(editCapability.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        lenient().when(editCapability.linearizationIds()).thenReturn(List.of(linearizationId));
        lenient().when(editCapability.contextCriteria()).thenReturn(editCriteria);
        lenient().when(editCapability.asGenericCapability()).thenReturn(editGenericCapability);
        lenient().when(editGenericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        // Setup view capability
        lenient().when(viewCapability.id()).thenReturn(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW);
        lenient().when(viewCapability.linearizationIds()).thenReturn(List.of(linearizationId));
        lenient().when(viewCapability.contextCriteria()).thenReturn(viewCriteria);
        lenient().when(viewCapability.asGenericCapability()).thenReturn(viewGenericCapability);
        lenient().when(viewGenericCapability.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(editCapability, viewCapability));

        // Both EDIT and VIEW are in matching keys
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW, LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(editCapability, LinearizationRowsCapability.class))
                .thenReturn(editCapability);
        when(objectMapper.convertValue(viewCapability, LinearizationRowsCapability.class))
                .thenReturn(viewCapability);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).contains(linearizationId);
        assertThat(result.readableLinearizations()).contains(linearizationId);
    }

    @Test
    void shouldHandleMultipleCapabilitiesWithSameId() throws ExecutionException, InterruptedException {
        // Given
        String linearizationId = "test-linearization";
        LinearizationRowsCapability capability1 = mock(LinearizationRowsCapability.class);
        LinearizationRowsCapability capability2 = mock(LinearizationRowsCapability.class);
        GenericParameterizedCapability genericCapability1 = mock(GenericParameterizedCapability.class);
        GenericParameterizedCapability genericCapability2 = mock(GenericParameterizedCapability.class);
        CompositeRootCriteria criteria1 = mock(CompositeRootCriteria.class);
        CompositeRootCriteria criteria2 = mock(CompositeRootCriteria.class);

        // Setup first capability
        lenient().when(capability1.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        lenient().when(capability1.linearizationIds()).thenReturn(List.of(linearizationId));
        lenient().when(capability1.contextCriteria()).thenReturn(criteria1);
        lenient().when(capability1.asGenericCapability()).thenReturn(genericCapability1);
        lenient().when(genericCapability1.type()).thenReturn(LinearizationRowsCapability.TYPE);

        // Setup second capability with same ID but different criteria
        lenient().when(capability2.id()).thenReturn(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW);
        lenient().when(capability2.linearizationIds()).thenReturn(List.of(linearizationId));
        lenient().when(capability2.contextCriteria()).thenReturn(criteria2);
        lenient().when(capability2.asGenericCapability()).thenReturn(genericCapability2);
        lenient().when(genericCapability2.type()).thenReturn(LinearizationRowsCapability.TYPE);

        GetAuthorizedCapabilitiesResponse authorizedResponse = mock(GetAuthorizedCapabilitiesResponse.class);
        when(authorizedResponse.capabilities()).thenReturn(Set.of(capability1, capability2));

        // Both criteria match
        GetMatchingCriteriaResponse matchingResponse = new GetMatchingCriteriaResponse(
                List.of(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW)
        );

        when(getAuthorizedActionsExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(authorizedResponse));
        when(getMatchingCriteriaExecutor.execute(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(matchingResponse));
        when(objectMapper.convertValue(capability1, LinearizationRowsCapability.class))
                .thenReturn(capability1);
        when(objectMapper.convertValue(capability2, LinearizationRowsCapability.class))
                .thenReturn(capability2);

        // When
        LinearizationDefinitionService.AllowedLinearizationDefinitions result =
                service.getUserAccessibleLinearizations(projectId, entityIri, allowedIds, executionContext);

        // Then
        assertThat(result.editableLinearizations()).contains(linearizationId);
        assertThat(result.readableLinearizations()).contains(linearizationId);
    }
} 