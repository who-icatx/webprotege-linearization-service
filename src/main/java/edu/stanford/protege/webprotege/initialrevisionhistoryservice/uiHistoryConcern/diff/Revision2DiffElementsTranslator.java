package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.diff;


import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.changes.LinearizationEventsForView;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.mappers.LinearizationEventMapper.groupEventsByViews;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
@Component
public class Revision2DiffElementsTranslator {

    public List<DiffElement<String, LinearizationEventsForView>> getDiffElementsFromRevision(List<LinearizationEvent> events, IRI entityIri) {
        final List<DiffElement<String, LinearizationEventsForView>> changeRecordElements = new ArrayList<>();
        final Map<String, List<LinearizationEvent>> eventsByViews = groupEventsByViews(events);

        eventsByViews.forEach((view, eventsInView) -> changeRecordElements.add(toElement(view, eventsInView, entityIri)));
        return changeRecordElements;
    }

    private DiffElement<String, LinearizationEventsForView> toElement(String viewName, List<LinearizationEvent> linearizationEvents, IRI entityIri) {
        var lineElement = LinearizationEventsForView.create(viewName, linearizationEvents);

        return new DiffElement<>(
                getDiffOperation(),
                entityIri.toQuotedString(),
                lineElement
        );
    }

    private DiffOperation getDiffOperation() {
        return DiffOperation.ADD;
    }
}
