package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff;

import edu.stanford.protege.webprotege.diff.*;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinition;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
public class Revision2DiffElementsTranslatorTest {

    private Revision2DiffElementsTranslator translator;

    @BeforeEach
    void setUp() {
        translator = new Revision2DiffElementsTranslator();
    }

    @Test
    public void GIVEN_eventsAndDefinitions_WHEN_getDiffElementsFromRevision_THEN_returnDiffElementsForMatchingView() {
        LinearizationEvent event1 = mock(LinearizationEvent.class);
        LinearizationEvent event2 = mock(LinearizationEvent.class);

        Map<String, List<LinearizationEvent>> eventsByViews = new HashMap<>();
        eventsByViews.put("http://some-view-iri", Arrays.asList(event1, event2));

        LinearizationDefinition def1 = new LinearizationDefinition("ID1",
                "http://some-view-iri",
                "oldId",
                "Description",
                "Mode",
                "Label",
                "rootId",
                "coreLinId",
                "01");
        LinearizationDefinition def2 = new LinearizationDefinition("ID2",
                "http://other-view-iri",
                "oldId2",
                "Description2",
                "Mode2",
                "Label2",
                "rootId2",
                "coreLinId2",
                "02");

        List<LinearizationDefinition> definitions = Arrays.asList(def1, def2);

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = translator.getDiffElementsFromRevision(eventsByViews, definitions);

        assertEquals(1, diffElements.size());
        DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement = diffElements.get(0);

        assertEquals(DiffOperation.ADD, diffElement.getDiffOperation());
        assertEquals("http://some-view-iri", diffElement.getSourceDocument().getLinearizationViewIri());
        assertEquals("Label", diffElement.getSourceDocument().getLinearizationViewName());
        assertEquals("ID1", diffElement.getSourceDocument().getLinearizationViewId());

        assertNotNull(diffElement.getLineElement());
        assertEquals(2, diffElement.getLineElement().getLinearizationEvents().size());
    }

    @Test
    public void GIVEN_residualEventWithoutMatchingDefinition_WHEN_getDiffElementsFromRevision_THEN_returnDiffElementWithResiduals() {
        LinearizationEvent event1 = mock(LinearizationEvent.class);

        Map<String, List<LinearizationEvent>> eventsByViews = new HashMap<>();
        eventsByViews.put("http://residual-view-iri", Collections.singletonList(event1));

        LinearizationDefinition def1 = new LinearizationDefinition("ID1",
                "http://some-view-iri",
                "oldId",
                "Description",
                "Mode",
                "Label",
                "rootId",
                "coreLinId",
                "01");

        List<LinearizationDefinition> definitions = Collections.singletonList(def1);

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = translator.getDiffElementsFromRevision(eventsByViews, definitions);

        assertEquals(1, diffElements.size());
        DiffElement<LinearizationDocumentChange, LinearizationEventsForView> diffElement = diffElements.get(0);

        assertEquals("Residuals", diffElement.getSourceDocument().getLinearizationViewName());
        assertEquals("ZZZZZZZZZZ", diffElement.getSourceDocument().getSortingCode());

        assertNotNull(diffElement.getLineElement());
        assertEquals(1, diffElement.getLineElement().getLinearizationEvents().size());
    }

    @Test
    public void GIVEN_multipleViewsAndEvents_WHEN_getDiffElementsFromRevision_THEN_returnCorrectNumberOfDiffElements() {
        LinearizationEvent event1 = mock(LinearizationEvent.class);
        LinearizationEvent event2 = mock(LinearizationEvent.class);
        LinearizationEvent event3 = mock(LinearizationEvent.class);

        Map<String, List<LinearizationEvent>> eventsByViews = new TreeMap<>();
        eventsByViews.put("http://view-iri-1", Arrays.asList(event1, event2));
        eventsByViews.put("http://view-iri-2", Collections.singletonList(event3));

        LinearizationDefinition def1 = new LinearizationDefinition("ID1",
                "http://view-iri-1",
                "oldId",
                "Description",
                "Mode",
                "Label1",
                "rootId",
                "coreLinId",
                "01");
        LinearizationDefinition def2 = new LinearizationDefinition("ID2",
                "http://view-iri-2",
                "oldId2",
                "Description2",
                "Mode2",
                "Label2",
                "rootId2",
                "coreLinId2",
                "02");

        List<LinearizationDefinition> definitions = Arrays.asList(def1, def2);

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = translator.getDiffElementsFromRevision(eventsByViews, definitions);

        assertEquals(2, diffElements.size());
        assertEquals("Label1", diffElements.get(0).getSourceDocument().getLinearizationViewName());
        assertEquals("Label2", diffElements.get(1).getSourceDocument().getLinearizationViewName());
    }

    @Test
    public void GIVEN_emptyEventsByViews_WHEN_getDiffElementsFromRevision_THEN_returnEmptyList() {
        Map<String, List<LinearizationEvent>> eventsByViews = new HashMap<>();
        List<LinearizationDefinition> definitions = new ArrayList<>();

        List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> diffElements = translator.getDiffElementsFromRevision(eventsByViews, definitions);

        assertTrue(diffElements.isEmpty());
    }
}
