package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationViewIriHelper.getLinearizationViewIris;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;

public class LinearizationEventHelper {

    public static Set<LinearizationEvent> getRandomLinearizationEvents() {
        Set<LinearizationEvent> linearizationEvents = new HashSet<>();
        getLinearizationViewIris().forEach(viewIri -> {
            linearizationEvents.add(getSetAuxAxisChildEvent(viewIri));
            linearizationEvents.add(getSetCodingNoteEvent(viewIri));
            linearizationEvents.add(getSetGroupingEvent(viewIri));
            linearizationEvents.add(getSetIncludedInLinearizationEvent(viewIri));
            linearizationEvents.add(getSetLinearizationParentEvent(viewIri));
        });

        linearizationEvents.add(getSetSuppressedSpecifiedResidualEvent());
        linearizationEvents.add(getSetUnspecifiedResidualTitleEvent());

        return linearizationEvents;
    }

    private static SetAuxiliaryAxisChild getSetAuxAxisChildEvent(String viewIRI) {
        return new SetAuxiliaryAxisChild(getRandomThreeStateBoolean(), viewIRI);
    }

    private static SetCodingNote getSetCodingNoteEvent(String viewIRI) {
        return new SetCodingNote(getRandomString(), viewIRI);
    }

    private static SetGrouping getSetGroupingEvent(String viewIRI) {
        return new SetGrouping(getRandomThreeStateBoolean(), viewIRI);
    }

    private static SetIncludedInLinearization getSetIncludedInLinearizationEvent(String viewIRI) {
        return new SetIncludedInLinearization(getRandomThreeStateBoolean(), viewIRI);
    }

    private static SetLinearizationParent getSetLinearizationParentEvent(String viewIRI) {
        return new SetLinearizationParent(getRandomIri(), viewIRI);
    }

    private static SetSuppressedSpecifiedResidual getSetSuppressedSpecifiedResidualEvent() {
        return new SetSuppressedSpecifiedResidual(getRandomThreeStateBoolean());
    }

    private static SetUnspecifiedResidualTitle getSetUnspecifiedResidualTitleEvent() {
        return new SetUnspecifiedResidualTitle(getRandomString());
    }


}
