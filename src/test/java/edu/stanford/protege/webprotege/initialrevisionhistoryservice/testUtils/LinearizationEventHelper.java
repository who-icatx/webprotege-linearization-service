package edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.LinearizationViewIriHelper.getLinearizationViewIris;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.getRandomIri;

public class LinearizationEventHelper {
    private static final Random RANDOM = new Random();
    private static final List<String> SAMPLE_STRINGS = List.of("Sample1", "Sample2", "Sample3");
    private static final List<ThreeStateBoolean> SAMPLE_BOOLS = List.of(ThreeStateBoolean.TRUE, ThreeStateBoolean.FALSE, ThreeStateBoolean.UNKNOWN);

    public static Set<LinearizationEvent> getRandomLinearizationEvents() {
        Set<LinearizationEvent> linearizationEvents = new HashSet<>();
        getLinearizationViewIris().forEach(viewIri -> {
            linearizationEvents.add(getSetAuxAxisChildEvent(viewIri));
            linearizationEvents.add(getSetCodingNoteEvent(viewIri));
            linearizationEvents.add(getSetGroupingEvent(viewIri));
            linearizationEvents.add(getSetIncludedInLinearizationEvent(viewIri));
            linearizationEvents.add(getSetLinearizationParentEvent(viewIri));
            linearizationEvents.add(getSetAuxAxisChildEvent(viewIri));
            linearizationEvents.add(getSetAuxAxisChildEvent(viewIri));
            linearizationEvents.add(getSetAuxAxisChildEvent(viewIri));
        });

        linearizationEvents.add(getSetSuppressedSpecifiedResidualEvent());
        linearizationEvents.add(getSetUnspecifiedResidualTitleEvent());

        return linearizationEvents;
    }

    private static SetAuxiliaryAxisChild getSetAuxAxisChildEvent(IRI viewIRI) {
        return new SetAuxiliaryAxisChild(getRandomThreeStateBoolean(), viewIRI);
    }

    private static SetCodingNote getSetCodingNoteEvent(IRI viewIRI) {
        return new SetCodingNote(getRandomString(), viewIRI);
    }

    private static SetGrouping getSetGroupingEvent(IRI viewIRI) {
        return new SetGrouping(getRandomThreeStateBoolean(), viewIRI);
    }

    private static SetIncludedInLinearization getSetIncludedInLinearizationEvent(IRI viewIRI) {
        return new SetIncludedInLinearization(getRandomThreeStateBoolean(), viewIRI);
    }

    private static SetLinearizationParent getSetLinearizationParentEvent(IRI viewIRI) {
        return new SetLinearizationParent(getRandomIri(), viewIRI);
    }

    private static SetSuppressedSpecifiedResidual getSetSuppressedSpecifiedResidualEvent() {
        return new SetSuppressedSpecifiedResidual(getRandomThreeStateBoolean());
    }

    private static SetUnspecifiedResidualTitle getSetUnspecifiedResidualTitleEvent() {
        return new SetUnspecifiedResidualTitle(getRandomString());
    }

    private static ThreeStateBoolean getRandomThreeStateBoolean() {
        return SAMPLE_BOOLS.get(RANDOM.nextInt(SAMPLE_BOOLS.size()));
    }

    private static String getRandomString() {
        return SAMPLE_STRINGS.get(RANDOM.nextInt(SAMPLE_STRINGS.size()));
    }


    public static Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        return linearizationSpecification.linearizationSpecifications().stream()
                .flatMap(specification -> {
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();

                    if (specification.getIsIncludedInLinearization() != null) {
                        response.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView()));
                    }
                    if (specification.getIsAuxiliaryAxisChild() != null) {
                        response.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView()));
                    }
                    if (specification.getLinearizationParent() != null) {
                        response.add(new SetLinearizationParent(specification.getLinearizationParent(), specification.getLinearizationView()));
                    }
                    if (specification.getIsGrouping() != null) {
                        response.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView()));
                    }
                    if (specification.getCodingNote() != null) {
                        response.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView()));
                    }

                    return response.stream();
                }).collect(Collectors.toSet());
    }

}
