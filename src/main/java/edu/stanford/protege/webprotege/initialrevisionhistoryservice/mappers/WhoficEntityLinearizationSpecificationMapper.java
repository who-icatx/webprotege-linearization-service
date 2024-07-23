package edu.stanford.protege.webprotege.initialrevisionhistoryservice.mappers;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
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
        ThreeStateBoolean suppressSpecifiedResidual = ThreeStateBoolean.UNKNOWN;
        String unspecifiedResidualTitle = "";

        return new LinearizationResiduals(suppressSpecifiedResidual, unspecifiedResidualTitle);
    }

    private List<LinearizationSpecification> extractDefaultSpecificationsFromSpec(List<LinearizationSpecification> linearizationSpecifications) {
        return linearizationSpecifications.stream()
                .map(spec ->
                        new LinearizationSpecification(
                                ThreeStateBoolean.UNKNOWN,
                                ThreeStateBoolean.UNKNOWN,
                                ThreeStateBoolean.FALSE,
                                IRI.create(""),
                                spec.getLinearizationView(),
                                ""
                        )
                )
                .collect(Collectors.toList());
    }
}
