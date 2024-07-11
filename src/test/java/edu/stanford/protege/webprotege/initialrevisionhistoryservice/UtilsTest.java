package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.*;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
class UtilsTest {


    @Test
    void GIVEN_entityLinearizationSpecification_WHEN_mappedToLinearizationEvent_allSpecificationEventsAreCreated() {
        IRI linearizationView = getRandomIri();
        IRI linearizationParent = getRandomIri();
        String codingNote = getRandomString();
        IRI entityIri = getRandomIri();
        LinearizationSpecification spec = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                linearizationParent,
                linearizationView,
                codingNote
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                entityIri,
                null,
                List.of(spec)
        );

        Set<LinearizationEvent> events = mapLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        assertEquals(5, events.size());
        events.forEach(event -> {
            if (event instanceof SetIncludedInLinearization includedInLinearizationEvent) {
                assertEquals(includedInLinearizationEvent.getValue(), spec.getIsIncludedInLinearization().name());
            } else if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent) {
                assertEquals(auxiliaryAxisChildEvent.getValue(), spec.getIsAuxiliaryAxisChild().name());
            } else if (event instanceof SetLinearizationParent linearizationParentEvent) {
                assertEquals(linearizationParentEvent.getValue(), spec.getLinearizationParent().toString());
            } else if (event instanceof SetGrouping setGroupingEvent) {
                assertEquals(setGroupingEvent.getValue(), spec.getIsGrouping().name());
            } else if (event instanceof SetCodingNote setCodingNoteEvent) {
                assertEquals(setCodingNoteEvent.getValue(), spec.getCodingNote());
            }
        });
    }

    @Test
    void GIVEN_entityLinearizationSpecification_WHEN_mappedToLinearizationEvent_allResidualEventsAreCreated() {
        String residualTitle = getRandomString();
        IRI entityIri = getRandomIri();

        LinearizationResiduals residuals = new LinearizationResiduals(
                ThreeStateBoolean.TRUE,
                residualTitle
        );

        WhoficEntityLinearizationSpecification specification = new WhoficEntityLinearizationSpecification(
                entityIri,
                residuals,
                List.of()
        );

        Set<LinearizationEvent> events = mapLinearizationResidualsEvents(specification);

        assertEquals(2, events.size());
        events.forEach(event -> {
            if (event instanceof SetSuppressedSpecifiedResidual suppressedSpecifiedResidualEvent) {
                assertEquals(suppressedSpecifiedResidualEvent.getValue(), residuals.getSuppressSpecifiedResidual().name());
            } else if (event instanceof SetUnspecifiedResidualTitle unspecifiedResidualTitleEvent) {
                assertEquals(unspecifiedResidualTitleEvent.getValue(), residuals.getUnspecifiedResidualTitle());
            }
        });
    }
}