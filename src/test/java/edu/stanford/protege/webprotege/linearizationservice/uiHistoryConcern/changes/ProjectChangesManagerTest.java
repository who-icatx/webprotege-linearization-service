package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.diff.*;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff.Revision2DiffElementsTranslator;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.nodeRendering.EntityRendererManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectChangesManagerTest {

    @Mock
    private Revision2DiffElementsTranslator revision2DiffElementsTranslator;
    @Mock
    private LinearizationDefinitionRepository definitionRepository;
    @Mock
    private EntityRendererManager entityRendererManager;
    @InjectMocks
    private ProjectChangesManager projectChangesManager;

    @Test
    public void GIVEN_validHistories_WHEN_getProjectChangesForHistoriesCalled_THEN_returnCorrectProjectChanges() {
        ProjectId projectId = new ProjectId("testProject");
        List<EntityLinearizationHistory> entityHistories = new ArrayList<>();

        LinearizationRevision revision = mock(LinearizationRevision.class);
        when(revision.userId()).thenReturn(UserId.valueOf("user1"));
        when(revision.timestamp()).thenReturn(12345L);
        when(revision.linearizationEvents()).thenReturn(Set.of(mock(LinearizationEvent.class)));

        LinearizationDocumentChange documentChange = mock(LinearizationDocumentChange.class);
        when(documentChange.getLinearizationViewName()).thenReturn("viewName");

        LinearizationEventsForView eventsForView = mock(LinearizationEventsForView.class);
        when(eventsForView.getLinearizationEvents()).thenReturn(List.of(mock(LinearizationEvent.class)));

        DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement =
                new DiffElement<>(DiffOperation.ADD, documentChange, eventsForView);

        EntityLinearizationHistory history = new EntityLinearizationHistory("iri1", "project1", Set.of(revision));
        entityHistories.add(history);

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = List.of(diffElement);
        when(revision2DiffElementsTranslator.getDiffElementsFromRevision(anyMap(), anyList())).thenReturn(diffElements);

        when(entityRendererManager.getRenderedEntities(anySet(), eq(projectId))).thenReturn(Collections.emptyList());
        when(definitionRepository.getLinearizationDefinitions()).thenReturn(Collections.emptyList());

        Set<ProjectChangeForEntity> result = projectChangesManager.getProjectChangesForHistories(projectId, entityHistories);

        assertNotNull(result);
        assertEquals(1, result.size());
        Iterator<ProjectChangeForEntity> iterator = result.iterator();

        ProjectChangeForEntity projectChangeForEntity = iterator.next();
        ChangeType projectChangeType = projectChangeForEntity.changeType();
        assertEquals(ChangeType.UPDATE_ENTITY, projectChangeType);

        ProjectChange projectChange = projectChangeForEntity.projectChange();
        assertNotNull(projectChange);
        assertEquals(12345L, projectChange.getTimestamp());
        assertEquals("user1", projectChange.getAuthor().id());

        assertNotNull(projectChange.getDiff());
        assertEquals(1, projectChange.getDiff().getPageElements().size());
    }


    @Test
    public void GIVEN_emptyHistories_WHEN_getProjectChangesForHistoriesCalled_THEN_returnEmptyList() {
        ProjectId projectId = new ProjectId("testProject");
        List<EntityLinearizationHistory> emptyHistories = Collections.emptyList();

        Set<ProjectChangeForEntity> result = projectChangesManager.getProjectChangesForHistories(projectId, emptyHistories);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void GIVEN_validProjectIdAndRevision_WHEN_getProjectChangesForRevisionCalled_THEN_returnCorrectProjectChange() {
        ProjectId projectId = new ProjectId("testProject");
        String entityIri = "http://example.com/entity";
        LinearizationRevision revision = mock(LinearizationRevision.class);

        when(revision.userId()).thenReturn(UserId.valueOf("user1"));
        when(revision.timestamp()).thenReturn(12345L);
        when(revision.linearizationEvents()).thenReturn(Set.of(mock(LinearizationEvent.class)));

        LinearizationEventsForView eventsForView = mock(LinearizationEventsForView.class);
        when(eventsForView.getLinearizationEvents()).thenReturn(List.of(mock(LinearizationEvent.class)));

        LinearizationDocumentChange documentChange = mock(LinearizationDocumentChange.class);
        when(documentChange.getLinearizationViewName()).thenReturn("viewName");

        DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement =
                new DiffElement<>(DiffOperation.ADD, documentChange, eventsForView);

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = List.of(diffElement);
        when(revision2DiffElementsTranslator.getDiffElementsFromRevision(anyMap(), anyList()))
                .thenReturn(diffElements);

        when(entityRendererManager.getRenderedEntities(anySet(), eq(projectId)))
                .thenReturn(Collections.emptyList());
        when(definitionRepository.getLinearizationDefinitions())
                .thenReturn(Collections.emptyList());

        ProjectChangeForEntity result = projectChangesManager.getProjectChangesForRevision(projectId, entityIri, revision, "");

        assertNotNull(result);
        assertEquals(entityIri, result.whoficEntityIri());
        assertEquals(ChangeType.UPDATE_ENTITY, result.changeType());

        ProjectChange projectChange = result.projectChange();
        assertNotNull(projectChange);
        assertEquals(12345L, projectChange.getTimestamp());
        assertEquals("user1", projectChange.getAuthor().id());

        assertNotNull(projectChange.getDiff());
        assertEquals(1, projectChange.getDiff().getPageElements().size());
    }

    @Test
    public void GIVEN_nullRevision_WHEN_getProjectChangesForRevisionCalled_THEN_throwException() {
        ProjectId projectId = new ProjectId("testProject");
        String entityIri = "http://example.com/entity";

        assertThrows(NullPointerException.class, () -> projectChangesManager.getProjectChangesForRevision(projectId, entityIri, null, null));
    }
}

