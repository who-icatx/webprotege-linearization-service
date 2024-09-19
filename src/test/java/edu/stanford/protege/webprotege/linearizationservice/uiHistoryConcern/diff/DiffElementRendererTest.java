package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff;

import edu.stanford.protege.webprotege.diff.*;
import edu.stanford.protege.webprotege.entity.EntityNode;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiffElementRendererTest {

    private DiffElementRenderer<String> renderer;
    private List<EntityNode> renderedEntities;

    @BeforeEach
    void setUp() {
        renderedEntities = mock(List.class);
        renderer = new DiffElementRenderer<>(renderedEntities);
    }

    @Test
    void GIVEN_linearizationDocumentChangeWithEvents_WHEN_renderCalled_THEN_returnCorrectRenderedElement() {
        var change = LinearizationDocumentChange.create("http://view-iri", "Label", "view-id", "01");
        var event1 = mock(SetAuxiliaryAxisChild.class);
        when(event1.accept(any())).thenReturn("Event1");
        var event2 = mock(SetCodingNote.class);
        when(event2.accept(any())).thenReturn("Event2");

        var eventsForView = LinearizationEventsForView.create("Label", List.of(event1, event2));
        var diffElement = new DiffElement<>(DiffOperation.ADD, change, eventsForView);

        var result = renderer.render(diffElement);

        assertEquals(DiffOperation.ADD, result.getDiffOperation());
        assertEquals("<span class=\"ms-quantifier-kw\">Label</span>", result.getSourceDocument());
        assertEquals("Event1Event2", result.getLineElement());
    }

    @Test
    void GIVEN_noLinearizationEvents_WHEN_renderCalled_THEN_returnRenderedElementWithNoValue() {
        var change = LinearizationDocumentChange.create("http://view-iri", "Label", "view-id", "01");
        var eventsForView = LinearizationEventsForView.create("Label", Collections.emptyList());
        var diffElement = new DiffElement<>(DiffOperation.ADD, change, eventsForView);

        var result = renderer.render(diffElement);

        assertEquals(DiffOperation.ADD, result.getDiffOperation());
        assertEquals("<span class=\"ms-quantifier-kw\">Label</span>", result.getSourceDocument());
        assertEquals("no value", result.getLineElement());
    }

    @Test
    void GIVEN_nullRenderedEntities_WHEN_renderCalled_THEN_returnRenderedElementWithOriginalValue() {
        renderer = new DiffElementRenderer<>(null);
        var change = LinearizationDocumentChange.create("http://view-iri", "Label", "view-id", "01");
        var event = mock(SetLinearizationParent.class);
        when(event.accept(any())).thenCallRealMethod();
        when(event.getUiDisplayName()).thenReturn("Parent");
        when(event.getValue()).thenReturn("parent-value");

        var eventsForView = LinearizationEventsForView.create("Label", List.of(event));
        var diffElement = new DiffElement<>(DiffOperation.ADD, change, eventsForView);

        var result = renderer.render(diffElement);

        assertEquals(DiffOperation.ADD, result.getDiffOperation());
        assertEquals("<span class=\"ms-quantifier-kw\">Label</span>", result.getSourceDocument());
        assertEquals("&nbsp;<span>Set the Parent value to <span class=\"ms-literal\">\"parent-value\"</span></span>;&nbsp;", result.getLineElement());
    }
}
