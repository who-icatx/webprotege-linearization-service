package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhoficEntityLinearizationSpecificationMapperTest {

    @Mock
    private LinearizationDefinitionRepository definitionRepository;

    @InjectMocks
    private WhoficEntityLinearizationSpecificationMapper mapper;

    @Test
    public void GIVEN_whoSpec_WHEN_mapToDefaultWhoficEntityLinearizationSpecification_THEN_defaultSpecCreated() {
        IRI newSpecIri = IRI.create(getRandomIri());
        IRI entityIri = IRI.create(getRandomIri());
        IRI linParentIri = IRI.create(getRandomIri());
        IRI linearizationView = IRI.create("http://id.who.int/icd/release/11/mms");
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                linParentIri,
                linearizationView,
                "codingNote"
        );
        WhoficEntityLinearizationSpecification whoficSpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                new LinearizationResiduals(LinearizationStateCell.TRUE, LinearizationStateCell.FALSE,"otherSpecifiedTitle", "residualTitle"),
                List.of(spec)
        );

        // Mock the repository to return a main linearization definition
        LinearizationDefinition mainDefinition = new LinearizationDefinition(
                "MMS", "http://id.who.int/icd/release/11/mms", "oldId", "description", 
                "LinMode.Basic", "displayLabel", "rootId", null, "sortingCode"
        );
        when(definitionRepository.getLinearizationDefinitions()).thenReturn(List.of(mainDefinition));

        WhoficEntityLinearizationSpecification result = mapper.mapToDefaultWhoficEntityLinearizationSpecification(newSpecIri, whoficSpec);

        assertEquals(newSpecIri, result.entityIRI());
        assertEquals(LinearizationStateCell.UNKNOWN, result.linearizationResiduals().getSuppressOtherSpecifiedResiduals());
        assertEquals("", result.linearizationResiduals().getUnspecifiedResidualTitle());

        List<LinearizationSpecification> defaultSpecifications = result.linearizationSpecifications();
        assertEquals(1, defaultSpecifications.size());

        LinearizationSpecification defaultSpec = defaultSpecifications.get(0);
        assertEquals(LinearizationStateCell.FALSE, defaultSpec.getIsAuxiliaryAxisChild());
        assertEquals(LinearizationStateCell.FALSE, defaultSpec.getIsGrouping());
        assertEquals(LinearizationStateCell.UNKNOWN, defaultSpec.getIsIncludedInLinearization());
        assertEquals(IRI.create(""), defaultSpec.getLinearizationParent());
        assertEquals(linearizationView, defaultSpec.getLinearizationView());
        assertEquals("", defaultSpec.getCodingNote());
    }

    @Test
    public void GIVEN_derivedLinearizationSpec_WHEN_mapToDefaultWhoficEntityLinearizationSpecification_THEN_derivedDefaultSpecCreated() {
        IRI newSpecIri = IRI.create(getRandomIri());
        IRI entityIri = IRI.create(getRandomIri());
        IRI derivedLinearizationView = IRI.create("http://id.who.int/icd/release/11/pcl");
        
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                derivedLinearizationView,
                "codingNote"
        );
        WhoficEntityLinearizationSpecification whoficSpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                new LinearizationResiduals(LinearizationStateCell.TRUE, LinearizationStateCell.FALSE,"otherSpecifiedTitle", "residualTitle"),
                List.of(spec)
        );

        // Mock the repository to return a derived linearization definition
        LinearizationDefinition derivedDefinition = new LinearizationDefinition(
                "PCL", "http://id.who.int/icd/release/11/pcl", "oldId", "description", 
                "LinMode.TelescopicFromAnotherLinearization", "displayLabel", "rootId", "MMS", "sortingCode"
        );
        when(definitionRepository.getLinearizationDefinitions()).thenReturn(List.of(derivedDefinition));

        WhoficEntityLinearizationSpecification result = mapper.mapToDefaultWhoficEntityLinearizationSpecification(newSpecIri, whoficSpec);

        assertEquals(newSpecIri, result.entityIRI());
        assertEquals(LinearizationStateCell.UNKNOWN, result.linearizationResiduals().getSuppressOtherSpecifiedResiduals());
        assertEquals("", result.linearizationResiduals().getUnspecifiedResidualTitle());

        List<LinearizationSpecification> defaultSpecifications = result.linearizationSpecifications();
        assertEquals(1, defaultSpecifications.size());

        LinearizationSpecification defaultSpec = defaultSpecifications.get(0);
        // Derived linearizations should use FOLLOW_BASE_LINEARIZATION for these fields
        assertEquals(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, defaultSpec.getIsAuxiliaryAxisChild());
        assertEquals(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, defaultSpec.getIsGrouping());
        assertEquals(LinearizationStateCell.UNKNOWN, defaultSpec.getIsIncludedInLinearization());
        assertEquals(IRI.create(""), defaultSpec.getLinearizationParent());
        assertEquals(derivedLinearizationView, defaultSpec.getLinearizationView());
        assertEquals("", defaultSpec.getCodingNote());
    }

    @Test
    public void GIVEN_mixedLinearizationSpecs_WHEN_mapToDefaultWhoficEntityLinearizationSpecification_THEN_correctDefaultsCreated() {
        IRI newSpecIri = IRI.create(getRandomIri());
        IRI entityIri = IRI.create(getRandomIri());
        IRI mainLinearizationView = IRI.create("http://id.who.int/icd/release/11/mms");
        IRI derivedLinearizationView = IRI.create("http://id.who.int/icd/release/11/pcl");
        
        LinearizationSpecification mainSpec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                mainLinearizationView,
                "mainCodingNote"
        );
        
        LinearizationSpecification derivedSpec = new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                IRI.create(getRandomIri()),
                derivedLinearizationView,
                "derivedCodingNote"
        );
        
        WhoficEntityLinearizationSpecification whoficSpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                new LinearizationResiduals(LinearizationStateCell.TRUE, LinearizationStateCell.FALSE,"otherSpecifiedTitle", "residualTitle"),
                List.of(mainSpec, derivedSpec)
        );

        // Mock the repository to return both main and derived definitions
        LinearizationDefinition mainDefinition = new LinearizationDefinition(
                "MMS", "http://id.who.int/icd/release/11/mms", "oldId", "description", 
                "LinMode.Basic", "displayLabel", "rootId", null, "sortingCode"
        );
        LinearizationDefinition derivedDefinition = new LinearizationDefinition(
                "PCL", "http://id.who.int/icd/release/11/pcl", "oldId", "description", 
                "LinMode.TelescopicFromAnotherLinearization", "displayLabel", "rootId", "MMS", "sortingCode"
        );
        when(definitionRepository.getLinearizationDefinitions()).thenReturn(List.of(mainDefinition, derivedDefinition));

        WhoficEntityLinearizationSpecification result = mapper.mapToDefaultWhoficEntityLinearizationSpecification(newSpecIri, whoficSpec);

        assertEquals(newSpecIri, result.entityIRI());
        List<LinearizationSpecification> defaultSpecifications = result.linearizationSpecifications();
        assertEquals(2, defaultSpecifications.size());

        // Find main specification
        LinearizationSpecification mainDefaultSpec = defaultSpecifications.stream()
                .filter(spec -> spec.getLinearizationView().equals(mainLinearizationView))
                .findFirst()
                .orElseThrow();
        
        // Find derived specification
        LinearizationSpecification derivedDefaultSpec = defaultSpecifications.stream()
                .filter(spec -> spec.getLinearizationView().equals(derivedLinearizationView))
                .findFirst()
                .orElseThrow();

        // Main linearization should have FALSE defaults
        assertEquals(LinearizationStateCell.FALSE, mainDefaultSpec.getIsAuxiliaryAxisChild());
        assertEquals(LinearizationStateCell.FALSE, mainDefaultSpec.getIsGrouping());
        assertEquals(LinearizationStateCell.UNKNOWN, mainDefaultSpec.getIsIncludedInLinearization());
        assertEquals(IRI.create(""), mainDefaultSpec.getLinearizationParent());
        assertEquals(mainLinearizationView, mainDefaultSpec.getLinearizationView());
        assertEquals("", mainDefaultSpec.getCodingNote());

        // Derived linearization should have FOLLOW_BASE_LINEARIZATION defaults
        assertEquals(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, derivedDefaultSpec.getIsAuxiliaryAxisChild());
        assertEquals(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, derivedDefaultSpec.getIsGrouping());
        assertEquals(LinearizationStateCell.UNKNOWN, derivedDefaultSpec.getIsIncludedInLinearization());
        assertEquals(IRI.create(""), derivedDefaultSpec.getLinearizationParent());
        assertEquals(derivedLinearizationView, derivedDefaultSpec.getLinearizationView());
        assertEquals("", derivedDefaultSpec.getCodingNote());
    }

    @Test
    public void GIVEN_linearizationDefinitionNotFound_WHEN_mapToDefaultWhoficEntityLinearizationSpecification_THEN_exceptionThrown() {
        IRI newSpecIri = IRI.create(getRandomIri());
        IRI entityIri = IRI.create(getRandomIri());
        IRI unknownLinearizationView = IRI.create("http://id.who.int/icd/release/11/unknown");
        
        LinearizationSpecification spec = new LinearizationSpecification(
                LinearizationStateCell.TRUE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.TRUE,
                IRI.create(getRandomIri()),
                unknownLinearizationView,
                "codingNote"
        );
        WhoficEntityLinearizationSpecification whoficSpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                new LinearizationResiduals(LinearizationStateCell.TRUE, LinearizationStateCell.FALSE,"otherSpecifiedTitle", "residualTitle"),
                List.of(spec)
        );

        // Mock the repository to return empty list
        when(definitionRepository.getLinearizationDefinitions()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> {
            mapper.mapToDefaultWhoficEntityLinearizationSpecification(newSpecIri, whoficSpec);
        });
    }

    @Test
    public void GIVEN_emptyLinearizationSpecifications_WHEN_mapToDefaultWhoficEntityLinearizationSpecification_THEN_emptyResult() {
        IRI newSpecIri = IRI.create(getRandomIri());
        IRI entityIri = IRI.create(getRandomIri());
        
        WhoficEntityLinearizationSpecification whoficSpec = new WhoficEntityLinearizationSpecification(
                entityIri,
                new LinearizationResiduals(LinearizationStateCell.TRUE, LinearizationStateCell.FALSE,"otherSpecifiedTitle", "residualTitle"),
                List.of()
        );

        when(definitionRepository.getLinearizationDefinitions()).thenReturn(List.of());

        WhoficEntityLinearizationSpecification result = mapper.mapToDefaultWhoficEntityLinearizationSpecification(newSpecIri, whoficSpec);

        assertEquals(newSpecIri, result.entityIRI());
        assertEquals(LinearizationStateCell.UNKNOWN, result.linearizationResiduals().getSuppressOtherSpecifiedResiduals());
        assertEquals("", result.linearizationResiduals().getUnspecifiedResidualTitle());
        assertTrue(result.linearizationSpecifications().isEmpty());
    }
}