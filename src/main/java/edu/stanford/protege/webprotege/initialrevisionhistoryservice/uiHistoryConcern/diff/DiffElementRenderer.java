package edu.stanford.protege.webprotege.initialrevisionhistoryservice.uiHistoryConcern.diff;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;

import java.io.Serializable;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
public class DiffElementRenderer<S extends Serializable> {

    public DiffElement<S, String> render(DiffElement<S, LinearizationEvent> element) {
        LinearizationEvent lineElement = element.getLineElement();
        return new DiffElement<>(
                element.getDiffOperation(),
                element.getSourceDocument(),
                renderData(lineElement)
        );
    }

    public String renderData(LinearizationEvent change) {
        return change.getValue();
    }
}
