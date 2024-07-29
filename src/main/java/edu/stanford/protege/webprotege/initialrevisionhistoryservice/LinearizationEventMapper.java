package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

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

    public Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification, WhoficEntityLinearizationSpecification oldWhoficSpecification) {
        return linearizationSpecification.linearizationSpecifications()
                .stream()
                .flatMap(specification -> {
                    LinearizationSpecification oldSpecification = getOldSpecification(oldWhoficSpecification, specification.getLinearizationView());
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();
                    addIncludedInLinearizationEvent(response, specification, oldSpecification);
                    addAuxiliaryAxisChildEvent(response, specification, oldSpecification);
                    addLinearizationParentEvent(response, specification, oldSpecification);
                    addGroupingEvent(response, specification, oldSpecification);
                    addCodingNoteEvent(response, specification, oldSpecification);
                    return response.stream();
                })
                .collect(Collectors.toSet());
    }


    private LinearizationSpecification getOldSpecification(WhoficEntityLinearizationSpecification oldWhoficSpecification, String linearizationView) {
        return oldWhoficSpecification.linearizationSpecifications().stream().filter(oldSpecification ->
            oldSpecification.getLinearizationView().equalsIgnoreCase(linearizationView)
        ).findFirst().orElse(null);
    }



    public Set<LinearizationEvent> mapLinearizationResidualsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        Set<LinearizationEvent> residuals = new HashSet<>();
        addSuppressedSpecifiedResidual(residuals, linearizationSpecification);
        addUnspecifiedTitleResidual(residuals, linearizationSpecification);
        return residuals;
    }

    public Set<LinearizationEvent> mapLinearizationResidualsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification, WhoficEntityLinearizationSpecification oldWhoficSpec) {
        Set<LinearizationEvent> residuals = new HashSet<>();
        addSuppressedSpecifiedResidual(residuals, linearizationSpecification, oldWhoficSpec.linearizationResiduals());
        addUnspecifiedTitleResidual(residuals, linearizationSpecification,  oldWhoficSpec.linearizationResiduals());
        return residuals;
    }

    private void addIncludedInLinearizationEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getIsIncludedInLinearization() != null) {
            events.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
        }
    }
    private void addIncludedInLinearizationEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if (specification.getIsIncludedInLinearization() != null && (oldSpecification == null || !oldSpecification.getIsIncludedInLinearization().equals(specification.getIsIncludedInLinearization()))) {
            events.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
        }
    }

    private void addAuxiliaryAxisChildEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getIsAuxiliaryAxisChild() != null) {
            events.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView().toString()));
        }
    }
    private void addAuxiliaryAxisChildEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if (specification.getIsAuxiliaryAxisChild() != null && (oldSpecification == null || !oldSpecification.getIsAuxiliaryAxisChild().equals(specification.getIsAuxiliaryAxisChild()))) {
            events.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView().toString()));
        }
    }
    private void addLinearizationParentEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getLinearizationParent() != null) {
            events.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
        }
    }
    private void addLinearizationParentEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if (specification.getLinearizationParent() != null && (oldSpecification == null || !oldSpecification.getLinearizationParent().equals(specification.getLinearizationParent()))) {
            events.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
        }
    }
    private void addGroupingEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getIsGrouping() != null) {
            events.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView().toString()));
        }
    }

    private void addGroupingEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if (specification.getIsGrouping() != null && (oldSpecification == null || !oldSpecification.getIsGrouping().equals(specification.getIsGrouping()))) {
            events.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView().toString()));
        }
    }

    private void addCodingNoteEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getCodingNote() != null) {
            events.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView().toString()));
        }
    }
    private void addCodingNoteEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if (specification.getCodingNote() != null && (oldSpecification == null || !oldSpecification.getCodingNote().equals(specification.getCodingNote()))) {
            events.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView().toString()));
        }
    }
    private void addSuppressedSpecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressSpecifiedResidual() != null) {
            events.add(new SetSuppressedSpecifiedResidual(specification.linearizationResiduals().getSuppressSpecifiedResidual()));
        }
    }
    private void addSuppressedSpecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressSpecifiedResidual() != null
                && (oldResiduals == null || oldResiduals.getSuppressSpecifiedResidual() == null || !oldResiduals.getSuppressSpecifiedResidual().equals(specification.linearizationResiduals().getSuppressSpecifiedResidual()))) {
            events.add(new SetSuppressedSpecifiedResidual(specification.linearizationResiduals().getSuppressSpecifiedResidual()));
        }
    }
    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getUnspecifiedResidualTitle() != null) {
            events.add(new SetUnspecifiedResidualTitle(specification.linearizationResiduals().getUnspecifiedResidualTitle()));
        }
    }

    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getUnspecifiedResidualTitle() != null
                && (oldResiduals == null || oldResiduals.getSuppressSpecifiedResidual() == null || !oldResiduals.getUnspecifiedResidualTitle().equals(specification.linearizationResiduals().getUnspecifiedResidualTitle()))) {
            events.add(new SetUnspecifiedResidualTitle(specification.linearizationResiduals().getUnspecifiedResidualTitle()));
        }
    }
}
