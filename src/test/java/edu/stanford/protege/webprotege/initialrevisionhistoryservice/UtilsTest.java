package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.*;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.EntityLinearizationHistoryHelper.getEntityLinearizationHistory;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void GIVEN_twoObjects_WHEN_oneIsNullAndTheOtherIsNot_THEN_isNotEqualReturnsTrue() {
        var string1 = getRandomString();
        String stringNull = null;

        var stringList = List.of(string1, getRandomString(), getRandomString());
        List<String> nullList = null;

        var history1 = getEntityLinearizationHistory(ProjectId.generate(), 3);
        EntityLinearizationHistory history2 = null;

        assertTrue(isNotEquals(string1,stringNull));
        assertTrue(isNotEquals(stringNull,string1));

        assertTrue(isNotEquals(stringList,nullList));
        assertTrue(isNotEquals(nullList,stringList));


        assertFalse(isNotEquals(null,null));
        assertFalse(isNotEquals(string1,string1));

        assertTrue(isNotEquals(history1, history2));
        assertTrue(isNotEquals(history2, history1));

        assertFalse(isNotEquals(history1, history1));
    }

    @Test
    void GIVEN_twoObjects_WHEN_theObjectsAreNotEqual_THEN_isNotEqualReturnsTrue() {
        String string1 = getRandomString();
        String string2 = getRandomString();

        var stringList1 = List.of(string1, string2, getRandomString());
        var stringList2 = List.of(string1, getRandomString(), string2);

        var history1 = getEntityLinearizationHistory(ProjectId.generate(), 3);
        var history2 = getEntityLinearizationHistory(ProjectId.generate(), 3);


        assertTrue(isNotEquals(string1, string2));
        assertTrue(isNotEquals(string2, string1));

        assertTrue(isNotEquals(stringList1, stringList2));
        assertTrue(isNotEquals(stringList2, stringList1));

        assertTrue(isNotEquals(history1, history2));
        assertTrue(isNotEquals(history2, history1));
    }

    @Test
    void GIVEN_twoObjects_WHEN_theObjectsAreEqual_THEN_isNotEqualReturnsFalse() {
        String string1 = getRandomString();
        String string2 = getRandomString();
        String string3 = getRandomString();

        var stringList1 = List.of(string1, string2, string3);
        var stringList2 = List.of(string1, string2, string3);

        var history1 = getEntityLinearizationHistory(ProjectId.generate(), 3);
        var history2 = history1;


        assertFalse(isNotEquals(string1, string1));

        assertFalse(isNotEquals(stringList1, stringList2));
        assertFalse(isNotEquals(stringList2, stringList1));

        assertFalse(isNotEquals(history1, history2));
        assertFalse(isNotEquals(history2, history1));
    }
}