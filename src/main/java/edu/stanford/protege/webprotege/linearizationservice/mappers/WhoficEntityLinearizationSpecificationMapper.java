package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WhoficEntityLinearizationSpecificationMapper {


    private final LinearizationDefinitionRepository definitionRepository;

    public WhoficEntityLinearizationSpecificationMapper(LinearizationDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    public WhoficEntityLinearizationSpecification mapToDefaultWhoficEntityLinearizationSpecification(IRI newSpecIri, WhoficEntityLinearizationSpecification whoficSpec) {
        List<LinearizationDefinition> linearizationDefinitions = definitionRepository.getLinearizationDefinitions();
        LinearizationResiduals residuals = getDefaultResiduals();
        List<LinearizationSpecification> specifications = extractDefaultSpecificationsFromSpec(whoficSpec.linearizationSpecifications(), linearizationDefinitions);

        return new WhoficEntityLinearizationSpecification(
                newSpecIri,
                residuals,
                specifications
        );
    }

    private LinearizationResiduals getDefaultResiduals() {
        LinearizationStateCell suppressOtherSpecifiedResidual = LinearizationStateCell.UNKNOWN;
        LinearizationStateCell suppressUnspecifiedSpecifiedResidual = LinearizationStateCell.UNKNOWN;

        String unspecifiedResidualTitle = "";
        String otherSpecifiedResidualTitle = "";

        return new LinearizationResiduals(suppressOtherSpecifiedResidual, suppressUnspecifiedSpecifiedResidual, otherSpecifiedResidualTitle, unspecifiedResidualTitle);
    }

    private List<LinearizationSpecification> extractDefaultSpecificationsFromSpec(List<LinearizationSpecification> linearizationSpecifications,
                                                                                  List<LinearizationDefinition> linearizationDefinitions) {
        return linearizationSpecifications.stream()
                .map(spec -> {
                        if (isDerived(spec, linearizationDefinitions)) {
                            return getDefaultDerivedSpecification(spec);
                        } else {
                            return getDefaultMainLinearizationSpecification(spec);
                        }
                    })
                .collect(Collectors.toList());
    }

    private LinearizationSpecification getDefaultDerivedSpecification(LinearizationSpecification spec) {
        return new LinearizationSpecification(
                LinearizationStateCell.FOLLOW_BASE_LINEARIZATION,
                LinearizationStateCell.FOLLOW_BASE_LINEARIZATION,
                LinearizationStateCell.UNKNOWN,
                IRI.create(""),
                spec.getLinearizationView(),
                ""
        );
    }

    private LinearizationSpecification getDefaultMainLinearizationSpecification(LinearizationSpecification spec) {
        return new LinearizationSpecification(
                LinearizationStateCell.FALSE,
                LinearizationStateCell.FALSE,
                LinearizationStateCell.UNKNOWN,
                IRI.create(""),
                spec.getLinearizationView(),
                ""
        );
    }

    private boolean isDerived(LinearizationSpecification specification, List<LinearizationDefinition> linearizationDefinitions) {
        LinearizationDefinition definition = linearizationDefinitions.stream()
                .filter(def -> def.getLinearizationUri().equals(specification.getLinearizationView().toString()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find linearization definition for " + specification.getLinearizationView().toString()));

        return definition.getCoreLinId() != null;
    }
}
