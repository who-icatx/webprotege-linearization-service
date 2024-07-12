package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    public static <T> boolean isNotNull(T object){
        return object != null;
    }


    public static Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        return linearizationSpecification.linearizationSpecifications().stream()
                .flatMap(specification -> {
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();

                    if (specification.getIsIncludedInLinearization() != null) {
                        response.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView()));
                    }
                    if (specification.getIsAuxiliaryAxisChild() != null) {
                        response.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView()));
                    }
                    if (specification.getLinearizationParent() != null) {
                        response.add(new SetLinearizationParent(specification.getLinearizationParent(), specification.getLinearizationView()));
                    }
                    if (specification.getIsGrouping() != null) {
                        response.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView()));
                    }
                    if (specification.getCodingNote() != null) {
                        response.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView()));
                    }

                    return response.stream();
                }).collect(Collectors.toSet());
    }

    public static Set<LinearizationEvent> mapLinearizationResidualsEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        Set<LinearizationEvent> residuals = new HashSet<>();

        if (linearizationSpecification.linearizationResiduals() != null) {
            if (linearizationSpecification.linearizationResiduals().getSuppressSpecifiedResidual() != null) {
                residuals.add(new SetSuppressedSpecifiedResidual(linearizationSpecification.linearizationResiduals().getSuppressSpecifiedResidual()));
            }
            if (linearizationSpecification.linearizationResiduals().getUnspecifiedResidualTitle() != null) {
                residuals.add(new SetUnspecifiedResidualTitle(linearizationSpecification.linearizationResiduals().getUnspecifiedResidualTitle()));
            }
        }
        return residuals;
    }
}
