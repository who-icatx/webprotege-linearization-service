package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class LinearizationEventMapperTest {

    private LinearizationEventMapper eventMapper;


    @Mock
    private LinearizationDefinitionRepository linearizationDefinitionRepository;

    @Before
    public void setUp() {
        eventMapper = new LinearizationEventMapper(linearizationDefinitionRepository);
    }

    @Test
    public void GIVEN_entityLinearizationSpecification_WHEN_mappedToLinearizationEvent_allSpecificationEventsAreCreated() {
        String linearizationView = getRandomIri();
        String linearizationParent = getRandomIri();
        String codingNote = getRandomString();
        String entityIri = getRandomIri();
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(linearizationParent),
                IRI.create(linearizationView),
                codingNote
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(entityIri),
                null,
                List.of(spec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

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
                LinearizationStateCell.TRUE,
                LinearizationStateCell.TRUE,
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

    @Test
    public void GIVEN_nonEmptyOldTitleAndDifferentNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_eventAdded() {
        String oldTitle = "Old Title";
        String newTitle = "New Title";

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        oldTitle,
                        null
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        newTitle,
                        null
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event instanceof SetOtherSpecifiedResidualTitle));
        assertEquals(newTitle, events.iterator().next().getValue());
    }

    @Test
    public void GIVEN_emptyOldTitleAndEmptyNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_noEventAdded() {
        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        "",
                        null
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        "",
                        null
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertTrue(events.isEmpty());
    }

    @Test
    public void GIVEN_nullOldTitleAndNonEmptyNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_eventAdded() {
        String newTitle = "New Title";

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        null
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        newTitle,
                        null
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event instanceof SetOtherSpecifiedResidualTitle));
        assertEquals(newTitle, events.iterator().next().getValue());
    }

    @Test
    public void GIVEN_nonEmptyOldTitleAndEmptyNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_eventAdded() {
        String oldTitle = "Old Title";

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        oldTitle,
                        null
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        "",
                        null
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event instanceof SetOtherSpecifiedResidualTitle));
        assertEquals("", events.iterator().next().getValue());
    }

    //----------------------------------------------------------------------------

    @Test
    public void GIVEN_nonEmptyOldUnspecifiedTitleAndDifferentNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_eventAdded() {
        String oldTitle = "Old Title";
        String newTitle = "New Title";

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        oldTitle
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        newTitle
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event instanceof SetUnspecifiedResidualTitle));
        assertEquals(newTitle, events.iterator().next().getValue());
    }

    @Test
    public void GIVEN_emptyOldUnspecifiedTitleAndEmptyNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_noEventAdded() {
        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        ""
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        ""
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertTrue(events.isEmpty());
    }

    @Test
    public void GIVEN_nullOldUnspecifiedTitleAndNonEmptyNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_eventAdded() {
        String newTitle = "New Title";

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        null
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        newTitle
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event instanceof SetUnspecifiedResidualTitle));
        assertEquals(newTitle, events.iterator().next().getValue());
    }

    @Test
    public void GIVEN_nonEmptyOldUnspecifiedTitleAndEmptyNewTitle_WHEN_mapLinearizationResidualsToEvents_called_THEN_eventAdded() {
        String oldTitle = "Old Title";

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        oldTitle
                ),
                List.of()
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                new LinearizationResiduals(
                        LinearizationStateCell.FALSE,
                        LinearizationStateCell.FALSE,
                        null,
                        ""
                ),
                List.of()
        );

        Set<LinearizationEvent> events = eventMapper.mapLinearizationResidualsToEvents(newSpec, oldSpec);

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event instanceof SetUnspecifiedResidualTitle));
        assertEquals("", events.iterator().next().getValue());
    }

}