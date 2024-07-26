package edu.stanford.protege.webprotege.initialrevisionhistoryservice.mappers;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LinearizationEventMapper {
    public Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        return linearizationSpecification.linearizationSpecifications()
                .stream()
                .flatMap(specification -> {
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();
                    addIncludedInLinearizationEvent(response, specification);
                    addAuxiliaryAxisChildEvent(response, specification);
                    addLinearizationParentEvent(response, specification);
                    addGroupingEvent(response, specification);
                    addCodingNoteEvent(response, specification);
                    return response.stream();
                })
                .collect(Collectors.toSet());
    }

    public Set<LinearizationEvent> mapLinearizationResidualsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        Set<LinearizationEvent> residuals = new HashSet<>();
        addSuppressedSpecifiedResidual(residuals, linearizationSpecification);
        addUnspecifiedTitleResidual(residuals, linearizationSpecification);
        return residuals;
    }

    private void addIncludedInLinearizationEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getIsIncludedInLinearization() != null) {
            events.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
        }
    }

    private void addAuxiliaryAxisChildEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getIsAuxiliaryAxisChild() != null) {
            events.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView().toString()));
        }
    }

    private void addLinearizationParentEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getLinearizationParent() != null) {
            events.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
        }
    }

    private void addGroupingEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getIsGrouping() != null) {
            events.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView().toString()));
        }
    }

    private void addCodingNoteEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getCodingNote() != null) {
            events.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView().toString()));
        }
    }

    private void addSuppressedSpecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressSpecifiedResidual() != null) {
            events.add(new SetSuppressedSpecifiedResidual(specification.linearizationResiduals().getSuppressSpecifiedResidual()));
        }
    }

    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getUnspecifiedResidualTitle() != null) {
            events.add(new SetUnspecifiedResidualTitle(specification.linearizationResiduals().getUnspecifiedResidualTitle()));
        }
    }
}
