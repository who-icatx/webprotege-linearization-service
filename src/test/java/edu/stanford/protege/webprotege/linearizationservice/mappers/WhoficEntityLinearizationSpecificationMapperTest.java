package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.IRI;

import java.util.List;

import static edu.stanford.protege.webprotege.linearizationservice.testUtils.RandomHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class WhoficEntityLinearizationSpecificationMapperTest {

    private final WhoficEntityLinearizationSpecificationMapper mapper = new WhoficEntityLinearizationSpecificationMapper();

    @Test
    public void GIVEN_whoSpec_WHEN_mapToDefaultWhoficEntityLinearizationSpecification_THEN_defaultSpecCreated() {
        IRI newSpecIri = IRI.create(getRandomIri());
        IRI entityIri = IRI.create(getRandomIri());
        IRI linParentIri = IRI.create(getRandomIri());
        IRI linearizationView = getRandomLinearizationView();
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
}