package edu.stanford.protege.webprotege.liniarizationservice.testUtils;

import edu.stanford.protege.webprotege.liniarizationservice.events.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.liniarizationservice.testUtils.LinearizationViewIriHelper.getLinearizationViewIris;
import static edu.stanford.protege.webprotege.liniarizationservice.testUtils.RandomHelper.*;

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

    private static SetAuxiliaryAxisChild getSetAuxAxisChildEvent(IRI viewIRI) {
        return new SetAuxiliaryAxisChild(getRandomThreeStateBoolean(), viewIRI.toString());
    }

    private static SetCodingNote getSetCodingNoteEvent(IRI viewIRI) {
        return new SetCodingNote(getRandomString(), viewIRI.toString());
    }

    private static SetGrouping getSetGroupingEvent(IRI viewIRI) {
        return new SetGrouping(getRandomThreeStateBoolean(), viewIRI.toString());
    }

    private static SetIncludedInLinearization getSetIncludedInLinearizationEvent(IRI viewIRI) {
        return new SetIncludedInLinearization(getRandomThreeStateBoolean(), viewIRI.toString());
    }

    private static SetLinearizationParent getSetLinearizationParentEvent(IRI viewIRI) {
        return new SetLinearizationParent(getRandomIri(), viewIRI.toString());
    }

    private static SetSuppressedOtherSpecifiedResidual getSetSuppressedSpecifiedResidualEvent() {
        return new SetSuppressedOtherSpecifiedResidual(getRandomThreeStateBoolean());
    }

    private static SetUnspecifiedResidualTitle getSetUnspecifiedResidualTitleEvent() {
        return new SetUnspecifiedResidualTitle(getRandomString());
    }


}
