package edu.stanford.protege.webprotege.linearizationservice.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.jackson.WebProtegeJacksonApplication;
import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import org.junit.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LinearizationEventMapperTest {

    private LinearizationEventMapper eventMapper;

    @Mock
    private LinearizationDefinitionRepository linearizationDefinitionRepository;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        ObjectMapper objectMapper = new WebProtegeJacksonApplication().objectMapper(new OWLDataFactoryImpl());

        FileInputStream fileInputStream = new FileInputStream("src/test/resources/LinearizationDefinitions.json");
        when(linearizationDefinitionRepository.getLinearizationDefinitions())
                .thenReturn(objectMapper.readValue(fileInputStream, new TypeReference<>() {
                }));
        eventMapper = new LinearizationEventMapper(linearizationDefinitionRepository);
    }

    @Test
    public void GIVEN_entityLinearizationSpecification_WHEN_mappedToLinearizationEvent_allSpecificationEventsAreCreated() {
        String linearizationView = "http://id.who.int/icd/release/11/mms";
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

    @Test
    public void GIVEN_derivedLinearizationSpec_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_followBaseLinearizationEventsCreated() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "codingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(spec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        assertEquals(5, events.size());
        
        // Check that auxiliary axis child is set to FOLLOW_BASE_LINEARIZATION for derived linearization
        events.forEach(event -> {
            if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", auxiliaryAxisChildEvent.getValue());
            } else if (event instanceof SetGrouping setGroupingEvent) {
                // Grouping should also be FOLLOW_BASE_LINEARIZATION for derived linearization
                assertEquals("FOLLOW_BASE_LINEARIZATION", setGroupingEvent.getValue());
            }
        });
    }

    @Test
    public void GIVEN_derivedLinearizationWithParentSpec_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_groupingFollowsParent() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        String parentLinearizationView = "http://id.who.int/icd/release/11/mms";
        
        LinearizationSpecification parentSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE, // Parent is grouping
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                IRI.create(parentLinearizationView),
                "parentCodingNote"
        );
        
        LinearizationSpecification derivedSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.TRUE, // Derived has same grouping as parent
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "derivedCodingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(parentSpec, derivedSpec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        // Should have 10 events (5 for each specification)
        assertEquals(10, events.size());
        
        // Check that derived linearization grouping follows parent
        events.forEach(event -> {
            if (event instanceof SetGrouping setGroupingEvent && 
                setGroupingEvent.getLinearizationView().equals(derivedLinearizationView)) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", setGroupingEvent.getValue());
            }
        });
    }

    @Test
    public void GIVEN_derivedLinearizationWithDifferentGroupingThanParent_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_derivedGroupingPreserved() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        String parentLinearizationView = "http://id.who.int/icd/release/11/mms";
        
        LinearizationSpecification parentSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE, // Parent is not grouping
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                IRI.create(parentLinearizationView),
                "parentCodingNote"
        );
        
        LinearizationSpecification derivedSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.TRUE, // Derived has different grouping than parent
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "derivedCodingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(parentSpec, derivedSpec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        // Should have 10 events (5 for each specification)
        assertEquals(10, events.size());
        
        // Check that derived linearization grouping is preserved when different from parent
        events.forEach(event -> {
            if (event instanceof SetGrouping setGroupingEvent && 
                setGroupingEvent.getLinearizationView().equals(derivedLinearizationView)) {
                assertEquals("TRUE", setGroupingEvent.getValue());
            }
        });
    }

    @Test
    public void GIVEN_derivedLinearizationWithNullGrouping_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_followBaseLinearizationUsed() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        String parentLinearizationView = "http://id.who.int/icd/release/11/mms";
        
        LinearizationSpecification parentSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                IRI.create(parentLinearizationView),
                "parentCodingNote"
        );
        
        LinearizationSpecification derivedSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                null, // Null grouping
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "derivedCodingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(parentSpec, derivedSpec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        // Check that derived linearization uses FOLLOW_BASE_LINEARIZATION when grouping is null
        events.forEach(event -> {
            if (event instanceof SetGrouping setGroupingEvent && 
                setGroupingEvent.getLinearizationView().equals(derivedLinearizationView)) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", setGroupingEvent.getValue());
            }
        });
    }

    @Test
    public void GIVEN_derivedLinearizationUpdate_WHEN_mapInitialLinearizationSpecificationsToEvents_called_THEN_correctEventsGenerated() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        String parentLinearizationView = "http://id.who.int/icd/release/11/mms";
        
        // Old specification
        LinearizationSpecification oldParentSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                IRI.create(parentLinearizationView),
                "oldParentCodingNote"
        );
        
        LinearizationSpecification oldDerivedSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "oldDerivedCodingNote"
        );

        WhoficEntityLinearizationSpecification oldSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(oldParentSpec, oldDerivedSpec)
        );

        // New specification - derived linearization now has FOLLOW_BASE_LINEARIZATION
        LinearizationSpecification newParentSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE, // Parent now has grouping
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                IRI.create(parentLinearizationView),
                "newParentCodingNote"
        );
        
        LinearizationSpecification newDerivedSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.TRUE, // Derived should follow parent
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "newDerivedCodingNote"
        );

        WhoficEntityLinearizationSpecification newSpec = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(newParentSpec, newDerivedSpec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(newSpec, oldSpec);

        // Check that derived linearization auxiliary axis child is set to FOLLOW_BASE_LINEARIZATION
        events.forEach(event -> {
            if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent && 
                auxiliaryAxisChildEvent.getLinearizationView().equals(derivedLinearizationView)) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", auxiliaryAxisChildEvent.getValue());
            }
        });
    }

    @Test
    public void GIVEN_derivedLinearizationWithoutParent_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_exceptionThrown() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        
        LinearizationSpecification derivedSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.TRUE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "derivedCodingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(derivedSpec)
        );

        // This should throw an exception because derived linearization needs a parent
        assertThrows(RuntimeException.class, () -> {
            eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);
        });
    }

    @Test
    public void GIVEN_mixedMainAndDerivedLinearizations_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_correctEventsForEach() {
        String mainLinearizationView = "http://id.who.int/icd/release/11/mms";
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        
        LinearizationSpecification mainSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(mainLinearizationView),
                "mainCodingNote"
        );
        
        LinearizationSpecification derivedSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "derivedCodingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(mainSpec, derivedSpec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        // Should have 10 events (5 for each specification)
        assertEquals(10, events.size());
        
        // Check main linearization events
        events.forEach(event -> {
            if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent && 
                auxiliaryAxisChildEvent.getLinearizationView().equals(mainLinearizationView)) {
                assertEquals("TRUE", auxiliaryAxisChildEvent.getValue());
            } else if (event instanceof SetGrouping setGroupingEvent && 
                setGroupingEvent.getLinearizationView().equals(mainLinearizationView)) {
                assertEquals("FALSE", setGroupingEvent.getValue());
            }
        });
        
        // Check derived linearization events
        events.forEach(event -> {
            if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent && 
                auxiliaryAxisChildEvent.getLinearizationView().equals(derivedLinearizationView)) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", auxiliaryAxisChildEvent.getValue());
            } else if (event instanceof SetGrouping setGroupingEvent && 
                setGroupingEvent.getLinearizationView().equals(derivedLinearizationView)) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", setGroupingEvent.getValue());
            }
        });
    }

    @Test
    public void GIVEN_derivedLinearizationWithNullAuxiliaryAxisChild_WHEN_mapInitialLinearizationSpecificationsToEvents_THEN_followBaseLinearizationUsed() {
        String derivedLinearizationView = "http://id.who.int/icd/release/11/pcl";
        
        LinearizationSpecification spec = new LinearizationSpecification(
                null, // Null auxiliary axis child
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(getRandomIri()),
                IRI.create(derivedLinearizationView),
                "codingNote"
        );

        WhoficEntityLinearizationSpecification entityLinearizationSpecification = new WhoficEntityLinearizationSpecification(
                IRI.create(getRandomIri()),
                null,
                List.of(spec)
        );

        Set<LinearizationEvent> events = eventMapper.mapInitialLinearizationSpecificationsToEvents(entityLinearizationSpecification);

        // Check that derived linearization uses FOLLOW_BASE_LINEARIZATION for auxiliary axis child
        events.forEach(event -> {
            if (event instanceof SetAuxiliaryAxisChild auxiliaryAxisChildEvent) {
                assertEquals("FOLLOW_BASE_LINEARIZATION", auxiliaryAxisChildEvent.getValue());
            }
        });
    }

}