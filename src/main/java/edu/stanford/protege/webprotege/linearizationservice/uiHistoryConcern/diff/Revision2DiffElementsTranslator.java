package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.diff;


import edu.stanford.protege.webprotege.diff.*;
import edu.stanford.protege.webprotege.linearizationservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinition;
import edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.changes.*;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class Revision2DiffElementsTranslator {

    public List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> getDiffElementsFromRevision(Map<String, List<LinearizationEvent>> eventsByViews,
                                                                                                                  List<LinearizationDefinition> linearizationDefinitions) {
        final List<DiffElement<LinearizationDocumentChange, LinearizationEventsForView>> changeRecordElements = new ArrayList<>();

        eventsByViews.forEach((view, eventsInView) -> changeRecordElements.add(toElement(view, eventsInView, linearizationDefinitions)));
        return changeRecordElements;
    }

    private DiffElement<LinearizationDocumentChange, LinearizationEventsForView> toElement(String viewName,
                                                                                           List<LinearizationEvent> linearizationEvents,
                                                                                           List<LinearizationDefinition> linearizationDefinitions) {
        var lineElement = LinearizationEventsForView.create(viewName, linearizationEvents);
        LinearizationDocumentChange sourceDocument;
        var optionalDefView = linearizationDefinitions.stream().filter(definition -> definition.getWhoficEntityIri().equals(viewName)).findFirst();
        if (optionalDefView.isPresent()) {
            var defView = optionalDefView.get();
            sourceDocument = LinearizationDocumentChange.create(defView.getWhoficEntityIri(), defView.getDisplayLabel(), defView.getId(), defView.getSortingCode());
        } else {
            sourceDocument = LinearizationDocumentChange.create(null, "Residuals", null, "ZZZZZZZZZZ");
        }
        return new DiffElement<>(
                getDiffOperation(),
                sourceDocument,
                lineElement
        );
    }

    private DiffOperation getDiffOperation() {
        return DiffOperation.ADD;
    }
}
