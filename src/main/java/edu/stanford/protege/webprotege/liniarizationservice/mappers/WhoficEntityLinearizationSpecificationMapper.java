package edu.stanford.protege.webprotege.liniarizationservice.mappers;

import edu.stanford.protege.webprotege.liniarizationservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WhoficEntityLinearizationSpecificationMapper {

    public WhoficEntityLinearizationSpecification mapToDefaultWhoficEntityLinearizationSpecification(IRI newSpecIri, WhoficEntityLinearizationSpecification whoficSpec) {
        LinearizationResiduals residuals = getDefaultResiduals();
        List<LinearizationSpecification> specifications = extractDefaultSpecificationsFromSpec(whoficSpec.linearizationSpecifications());

        return new WhoficEntityLinearizationSpecification(
                newSpecIri,
                residuals,
                specifications
        );
    }

    private LinearizationResiduals getDefaultResiduals() {
        ThreeStateBoolean suppressOtherSpecifiedResidual = ThreeStateBoolean.UNKNOWN;
        ThreeStateBoolean suppressUnspecifiedSpecifiedResidual = ThreeStateBoolean.UNKNOWN;

        String unspecifiedResidualTitle = "";
        String otherSpecifiedResidualTitle = "";

        return new LinearizationResiduals(suppressOtherSpecifiedResidual, suppressUnspecifiedSpecifiedResidual,otherSpecifiedResidualTitle, unspecifiedResidualTitle);
    }

    private List<LinearizationSpecification> extractDefaultSpecificationsFromSpec(List<LinearizationSpecification> linearizationSpecifications) {
        return linearizationSpecifications.stream()
                .map(spec ->
                        new LinearizationSpecification(
                                ThreeStateBoolean.FALSE,
                                ThreeStateBoolean.FALSE,
                                ThreeStateBoolean.UNKNOWN,
                                IRI.create(""),
                                spec.getLinearizationView(),
                                ""
                        )
                )
                .collect(Collectors.toList());
    }
}
