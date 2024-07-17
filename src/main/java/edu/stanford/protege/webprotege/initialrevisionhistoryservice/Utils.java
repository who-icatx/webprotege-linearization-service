package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {

    public static <T> boolean isNotEquals(T a, T b) {
        if (a == null && b == null) {
            return false;
        } else if (a == null) {
            return true;
        } else if (b == null) {
            return true;
        }
        return !a.equals(b);
    }

    public static <T extends Collection> boolean isNotEmpty(T object) {
        return !object.isEmpty();
    }

    public static Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        return linearizationSpecification.linearizationSpecifications().stream()
                .flatMap(specification -> {
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();

                    if (specification.getIsIncludedInLinearization() != null) {
                        response.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
                    }
                    if (specification.getIsAuxiliaryAxisChild() != null) {
                        response.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView().toString()));
                    }
                    if (specification.getLinearizationParent() != null) {
                        response.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
                    }
                    if (specification.getIsGrouping() != null) {
                        response.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView().toString()));
                    }
                    if (specification.getCodingNote() != null) {
                        response.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView().toString()));
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
