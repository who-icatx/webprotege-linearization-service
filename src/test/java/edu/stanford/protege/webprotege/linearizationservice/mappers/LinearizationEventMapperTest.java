package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LinearizationEventMapperTest {

    private LinearizationEventMapper eventMapper;

    @Before
    public void setUp() {
        eventMapper = new LinearizationEventMapper();
    }

    @Test
    public void GIVEN_entityLinearizationSpecification_WHEN_mappedToLinearizationEvent_allSpecificationEventsAreCreated() {
        String linearizationView = getRandomIri();
        String linearizationParent = getRandomIri();
        String codingNote = getRandomString();
        String entityIri = getRandomIri();
        LinearizationSpecification spec = new LinearizationSpecification(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.FALSE,
                ThreeStateBoolean.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                null,
                List.of(spec)
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationSpecificationsToEvents(entityLinearizationSpecification);

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
    public void GIVEN_entityLinearizationSpecification_WHEN_mappedToLinearizationEvent_allResidualEventsAreCreated() {
        String residualTitle = getRandomString();
        IRI entityIri = IRI.create(getRandomIri());

        LinearizationResiduals residuals = new LinearizationResiduals(
                ThreeStateBoolean.TRUE,
                ThreeStateBoolean.TRUE,
                residualTitle,
                getRandomString()
        );

        WhoficEntityLinearizationSpecification specification = new WhoficEntityLinearizationSpecification(
                entityIri,
                residuals,
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(specification);

        assertEquals(4, events.size());
        events.forEach(event -> {
            if (event instanceof SetSuppressedOtherSpecifiedResidual suppressedSpecifiedResidualEvent) {
                assertEquals(suppressedSpecifiedResidualEvent.getValue(), residuals.getSuppressOtherSpecifiedResiduals().name());
            } else if (event instanceof SetSuppressedUnspecifiedResiduals supressUnspecifiedResiduals) {
                assertEquals(supressUnspecifiedResiduals.getValue(), residuals.getSuppressUnspecifiedResiduals().name());
            } else if (event instanceof SetUnspecifiedResidualTitle unspecifiedResidualTitleEvent) {
                assertEquals(unspecifiedResidualTitleEvent.getValue(), residuals.getUnspecifiedResidualTitle());
            } else if (event instanceof SetOtherSpecifiedResidualTitle otherSpecifiedResidualTitle) {
                assertEquals(otherSpecifiedResidualTitle.getValue(), residuals.getOtherSpecifiedResidualTitle());
            }
        });
    }
}