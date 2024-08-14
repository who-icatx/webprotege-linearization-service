package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LinearizationEventMapper {
    public Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        if(linearizationSpecification.linearizationSpecifications()!=null){
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
        return Set.of();
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


    private LinearizationSpecification getOldSpecification(WhoficEntityLinearizationSpecification oldWhoficSpecification, IRI linearizationView) {
        return oldWhoficSpecification.linearizationSpecifications().stream().filter(oldSpecification ->
            oldSpecification.getLinearizationView().equals(linearizationView)
        ).findFirst().orElse(null);
    }



    public Set<LinearizationEvent> mapLinearizationResidualsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        Set<LinearizationEvent> residuals = new HashSet<>();
        addSuppressedOtherSpecifiedResidual(residuals, linearizationSpecification);
        addSuppressedUnspecifiedResidual(residuals, linearizationSpecification);
        addUnspecifiedTitleResidual(residuals, linearizationSpecification);
        addOtherSpecifiedTitleResidual(residuals, linearizationSpecification);
        return residuals;
    }

    public Set<LinearizationEvent> mapLinearizationResidualsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification, WhoficEntityLinearizationSpecification oldWhoficSpec) {
        Set<LinearizationEvent> residuals = new HashSet<>();
        addSuppressedOtherSpecifiedResidual(residuals, linearizationSpecification, oldWhoficSpec.linearizationResiduals());
        addSuppressedUnspecifiedResidual(residuals, linearizationSpecification, oldWhoficSpec.linearizationResiduals());
        addUnspecifiedTitleResidual(residuals, linearizationSpecification,  oldWhoficSpec.linearizationResiduals());
        addOtherSpecifiedTitleResidual(residuals, linearizationSpecification, oldWhoficSpec.linearizationResiduals());
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
        if (specification.getLinearizationParent() != null && !specification.getLinearizationParent().isEmpty()) {
            events.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
        }
    }
    private void addLinearizationParentEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if ((specification.getLinearizationParent() != null && !specification.getLinearizationParent().isEmpty())
                && (oldSpecification == null || oldSpecification.getLinearizationParent() == null || !oldSpecification.getLinearizationParent().equals(specification.getLinearizationParent()))) {
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
        if ((specification.getCodingNote() != null && !specification.getCodingNote().isEmpty()) && (oldSpecification == null || !specification.getCodingNote().equalsIgnoreCase(oldSpecification.getCodingNote()))) {
            events.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView().toString()));
        }
    }
    private void addSuppressedOtherSpecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals() != null) {
            events.add(new SetSuppressedOtherSpecifiedResidual(specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals()));
        }
    }
    private void addSuppressedOtherSpecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals() != null
                && (oldResiduals == null || oldResiduals.getSuppressOtherSpecifiedResiduals() == null || !oldResiduals.getSuppressOtherSpecifiedResiduals().equals(specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals()))) {
            events.add(new SetSuppressedOtherSpecifiedResidual(specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals()));
        }
    }


    private void addSuppressedUnspecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals() != null) {
            events.add(new SetSuppressedUnspecifiedResiduals(specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals()));
        }
    }
    private void addSuppressedUnspecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressUnspecifiedResiduals() != null
                && (oldResiduals == null || oldResiduals.getSuppressUnspecifiedResiduals() == null || !oldResiduals.getSuppressUnspecifiedResiduals().equals(specification.linearizationResiduals().getSuppressUnspecifiedResiduals()))) {
            events.add(new SetSuppressedUnspecifiedResiduals(specification.linearizationResiduals().getSuppressUnspecifiedResiduals()));
        }
    }



    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null) {
            events.add(new SetUnspecifiedResidualTitle(specification.linearizationResiduals().getUnspecifiedResidualTitle()));
        }
    }

    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null
                && (oldResiduals == null || oldResiduals.getUnspecifiedResidualTitle() == null || !oldResiduals.getUnspecifiedResidualTitle().equals(specification.linearizationResiduals().getUnspecifiedResidualTitle()))) {
            events.add(new SetUnspecifiedResidualTitle(specification.linearizationResiduals().getUnspecifiedResidualTitle()));
        }
    }


    private void addOtherSpecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getOtherSpecifiedResidualTitle() != null) {
            events.add(new SetOtherSpecifiedResidualTitle(specification.linearizationResiduals().getOtherSpecifiedResidualTitle()));
        }
    }

    private void addOtherSpecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null &&
                (oldResiduals == null || oldResiduals.getOtherSpecifiedResidualTitle() == null || !oldResiduals.getOtherSpecifiedResidualTitle().equals(specification.linearizationResiduals().getOtherSpecifiedResidualTitle()))) {
            events.add(new SetOtherSpecifiedResidualTitle(specification.linearizationResiduals().getOtherSpecifiedResidualTitle()));
        }
    }

    public static Map<String, List<LinearizationEvent>> groupEventsByViews(List<LinearizationEvent> events) {
        return events.stream().collect(Collectors.groupingBy(event -> {
            if (event instanceof LinearizationSpecificationEvent) {
                return ((LinearizationSpecificationEvent) event).getLinearizationView();
            } else {
                return "residualEvents";
            }
        }));
    }
}
