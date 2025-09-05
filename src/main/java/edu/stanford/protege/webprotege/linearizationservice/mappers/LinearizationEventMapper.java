package edu.stanford.protege.webprotege.linearizationservice.mappers;

import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import edu.stanford.protege.webprotege.linearizationservice.repositories.definitions.LinearizationDefinitionRepository;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class LinearizationEventMapper {

    private final LinearizationDefinitionRepository linearizationDefinitionRepository;
    private final static Logger LOGGER = LoggerFactory.getLogger(LinearizationEventMapper.class);

    public LinearizationEventMapper(LinearizationDefinitionRepository linearizationDefinitionRepository) {
        this.linearizationDefinitionRepository = linearizationDefinitionRepository;
    }

    public Set<LinearizationEvent> mapInitialLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        var linearizationDefinitions = linearizationDefinitionRepository.getLinearizationDefinitions();

        if (linearizationSpecification.linearizationSpecifications() != null) {
            return linearizationSpecification.linearizationSpecifications()
                    .stream()
                    .flatMap(specification -> {
                        Optional<LinearizationSpecification> parentSpecification = getParentSpecification(specification,
                                linearizationSpecification.linearizationSpecifications(),
                                linearizationDefinitions);
                        List<LinearizationSpecificationEvent> response = new ArrayList<>();
                        addIncludedInLinearizationEvent(response, specification, linearizationDefinitions, parentSpecification);
                        addAuxiliaryAxisChildEvent(response, specification, linearizationDefinitions);
                        addLinearizationParentEvent(response, specification);
                        addGroupingEvent(response, specification, linearizationDefinitions, parentSpecification);
                        addCodingNoteEvent(response, specification);
                        return response.stream();
                    })
                    .collect(Collectors.toSet());
        }
        LOGGER.info("Entity {} has no specifications", linearizationSpecification.entityIRI().toString());
        return Set.of();
    }

    private Optional<LinearizationSpecification> getParentSpecification(LinearizationSpecification specification,
                                                                        List<LinearizationSpecification> linearizationSpecifications,
                                                                        List<LinearizationDefinition> linearizationDefinitions) {
        LinearizationDefinition definition = linearizationDefinitions.stream()
                .filter(def -> def.getLinearizationUri().equals(specification.getLinearizationView().toString()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find linearization definition for " +specification.getLinearizationView().toString()));
        if(definition.getCoreLinId() == null) {
            return Optional.empty();
        }
        LinearizationDefinition parentDefinition = linearizationDefinitions.stream()
                .filter(def -> def.getLinearizationId().equals(definition.getCoreLinId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find parent definition for " +definition.getLinearizationId()));

        return linearizationSpecifications.stream()
                .filter(parentSpec -> parentSpec.getLinearizationView().toString().equals(parentDefinition.getLinearizationUri()))
                .findFirst();
    }

    public Set<LinearizationEvent> mapInitialLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification, WhoficEntityLinearizationSpecification oldWhoficSpecification) {

        var linearizationDefinitions = linearizationDefinitionRepository.getLinearizationDefinitions();

        return linearizationSpecification.linearizationSpecifications()
                .stream()
                .flatMap(specification -> {
                    LinearizationSpecification oldSpecification = getOldSpecification(oldWhoficSpecification, specification.getLinearizationView());
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();
                    addIncludedInLinearizationEvent(response, specification, oldSpecification);
                    addAuxiliaryAxisChildEvent(response, specification, oldSpecification, linearizationDefinitions);
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
        addUnspecifiedTitleResidual(residuals, linearizationSpecification, oldWhoficSpec.linearizationResiduals());
        addOtherSpecifiedTitleResidual(residuals, linearizationSpecification, oldWhoficSpec.linearizationResiduals());
        return residuals;
    }

    private void addIncludedInLinearizationEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, List<LinearizationDefinition> linearizationDefinitions, Optional<LinearizationSpecification> parentSpecification) {
         var isDerived = isDerived(specification, linearizationDefinitions);

        if(!isDerived) {
            if(specification.getIsIncludedInLinearization() != null) {
                events.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
            } else {
                events.add(new SetIncludedInLinearization(LinearizationStateCell.UNKNOWN, specification.getLinearizationView().toString()));
            }
        } else {
            LinearizationSpecification parentSpec = parentSpecification.orElseThrow(() -> new RuntimeException("Parent is not present for derived class " + specification.getLinearizationView()));
            if(specification.getIsIncludedInLinearization() != null && parentSpec.getIsIncludedInLinearization() != null) {
                if(parentSpecification.get().getIsIncludedInLinearization().equals(specification.getIsIncludedInLinearization()) || LinearizationStateCell.UNKNOWN.equals(specification.getIsIncludedInLinearization())) {
                    events.add(new SetIncludedInLinearization(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, specification.getLinearizationView().toString()));
                } else {
                    events.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
                }
            } else {
                events.add(new SetIncludedInLinearization(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, specification.getLinearizationView().toString()));
            }

        }
    }

    private void addIncludedInLinearizationEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if (specification.getIsIncludedInLinearization() != null && (oldSpecification == null || !oldSpecification.getIsIncludedInLinearization().equals(specification.getIsIncludedInLinearization()))) {
            events.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView().toString()));
        }
    }

    private void addAuxiliaryAxisChildEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, List<LinearizationDefinition> definitions) {
        boolean isDerived = isDerived(specification, definitions);

        if(!isDerived) {
            if(specification.getIsAuxiliaryAxisChild() != null) {
                events.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView().toString()));
            } else {
                events.add(new SetAuxiliaryAxisChild(LinearizationStateCell.UNKNOWN, specification.getLinearizationView().toString()));
            }
        } else {
            events.add(new SetAuxiliaryAxisChild(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, specification.getLinearizationView().toString()));
        }
    }

    private void addAuxiliaryAxisChildEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification, List<LinearizationDefinition> definitions) {
        if(!isDerived(specification, definitions)) {
            if (specification.getIsAuxiliaryAxisChild() != null && (oldSpecification == null || !oldSpecification.getIsAuxiliaryAxisChild().equals(specification.getIsAuxiliaryAxisChild()))) {
                events.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView().toString()));
            }
        } else {
            if(!LinearizationStateCell.FOLLOW_BASE_LINEARIZATION.equals(oldSpecification.getIsAuxiliaryAxisChild())) {
                events.add(new SetAuxiliaryAxisChild(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, specification.getLinearizationView().toString()));
            }
        }


    }

    private void addLinearizationParentEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification) {
        if (specification.getLinearizationParent() != null && !specification.getLinearizationParent().isEmpty()) {
            events.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
        }
    }

    private void addLinearizationParentEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification, LinearizationSpecification oldSpecification) {
        if ((specification.getLinearizationParent() != null) &&
                (oldSpecification == null ||
                        (oldSpecification.getLinearizationParent() == null &&
                                !specification.getLinearizationParent().toString().equals("")) ||
                        (oldSpecification.getLinearizationParent() != null &&
                                !oldSpecification.getLinearizationParent().equals(specification.getLinearizationParent()))
                )
        ) {
            events.add(new SetLinearizationParent(specification.getLinearizationParent().toString(), specification.getLinearizationView().toString()));
        }
    }

    private void addGroupingEvent(List<LinearizationSpecificationEvent> events, LinearizationSpecification specification,
                                  List<LinearizationDefinition> definitions,
                                  Optional<LinearizationSpecification> parentSpecification) {
        var isDerived = isDerived(specification, definitions);

        if(!isDerived) {
            if(specification.getIsGrouping() != null) {
                events.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView().toString()));
            } else {
                events.add(new SetGrouping(LinearizationStateCell.UNKNOWN, specification.getLinearizationView().toString()));
            }
        } else {
            LinearizationSpecification parentSpec = parentSpecification.orElseThrow(() -> new RuntimeException("Parent is not present for derived class " + specification.getLinearizationView()));
            if(specification.getIsGrouping() != null && parentSpec.getIsGrouping() != null) {
                if(parentSpecification.get().getIsGrouping().equals(specification.getIsGrouping()) || LinearizationStateCell.UNKNOWN.equals(specification.getIsGrouping())) {
                    events.add(new SetGrouping(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, specification.getLinearizationView().toString()));
                } else {
                    events.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView().toString()));
                }
            } else {
                events.add(new SetGrouping(LinearizationStateCell.FOLLOW_BASE_LINEARIZATION, specification.getLinearizationView().toString()));
            }

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
        } else {
            events.add(new SetSuppressedOtherSpecifiedResidual(LinearizationStateCell.UNKNOWN));
        }
    }

    private void addSuppressedOtherSpecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals() != null) {
            LinearizationStateCell newSuppressValue = specification.linearizationResiduals().getSuppressOtherSpecifiedResiduals();

            boolean shouldSaveUnknown = newSuppressValue.equals(LinearizationStateCell.UNKNOWN) &&
                    oldResiduals != null &&
                    (oldResiduals.getSuppressOtherSpecifiedResiduals() == null ||
                            (oldResiduals.getSuppressOtherSpecifiedResiduals() != null &&
                                    !oldResiduals.getSuppressOtherSpecifiedResiduals().equals(LinearizationStateCell.UNKNOWN)));

            boolean shouldSaveValue = shouldSaveUnknown ||
                    (!newSuppressValue.equals(LinearizationStateCell.UNKNOWN) &&
                            (oldResiduals == null ||
                                    oldResiduals.getSuppressOtherSpecifiedResiduals() == null ||
                                    !oldResiduals.getSuppressOtherSpecifiedResiduals().equals(newSuppressValue)));

            if (shouldSaveValue) {
                events.add(new SetSuppressedOtherSpecifiedResidual(newSuppressValue));
            }
        }
    }


    private void addSuppressedUnspecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressUnspecifiedResiduals() != null) {
            events.add(new SetSuppressedUnspecifiedResiduals(specification.linearizationResiduals().getSuppressUnspecifiedResiduals()));
        } else {
            events.add(new SetSuppressedUnspecifiedResiduals(LinearizationStateCell.UNKNOWN));
        }
    }

    private void addSuppressedUnspecifiedResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getSuppressUnspecifiedResiduals() != null) {
            LinearizationStateCell newSuppressValue = specification.linearizationResiduals().getSuppressUnspecifiedResiduals();

            boolean shouldSaveUnknown = newSuppressValue.equals(LinearizationStateCell.UNKNOWN) &&
                    oldResiduals != null &&
                    (oldResiduals.getSuppressUnspecifiedResiduals() == null ||
                            (oldResiduals.getSuppressUnspecifiedResiduals() != null &&
                                    !oldResiduals.getSuppressUnspecifiedResiduals().equals(LinearizationStateCell.UNKNOWN)));

            boolean shouldSaveValue = shouldSaveUnknown ||
                    (!newSuppressValue.equals(LinearizationStateCell.UNKNOWN) &&
                            (oldResiduals == null ||
                                    oldResiduals.getSuppressUnspecifiedResiduals() == null ||
                                    !oldResiduals.getSuppressUnspecifiedResiduals().equals(newSuppressValue)));

            if (shouldSaveValue) {
                events.add(new SetSuppressedUnspecifiedResiduals(newSuppressValue));
            }
        }
    }


    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null) {
            events.add(new SetUnspecifiedResidualTitle(specification.linearizationResiduals().getUnspecifiedResidualTitle()));
        }
    }

    private void addUnspecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null) {
            String newTitle = specification.linearizationResiduals().getUnspecifiedResidualTitle();
            String oldTitle = oldResiduals != null ? oldResiduals.getUnspecifiedResidualTitle() : null;

            if (shouldAddNewTitle(newTitle, oldTitle) ||
                    shouldSaveEmptyString(newTitle, oldTitle)) {
                events.add(new SetUnspecifiedResidualTitle(newTitle));
            }
        }
    }


    private void addOtherSpecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification) {
        if (specification.linearizationResiduals() != null && specification.linearizationResiduals().getOtherSpecifiedResidualTitle() != null) {
            events.add(new SetOtherSpecifiedResidualTitle(specification.linearizationResiduals().getOtherSpecifiedResidualTitle()));
        }
    }

    private void addOtherSpecifiedTitleResidual(Set<LinearizationEvent> events, WhoficEntityLinearizationSpecification specification, LinearizationResiduals oldResiduals) {
        if (specification.linearizationResiduals() != null) {
            String newTitle = specification.linearizationResiduals().getOtherSpecifiedResidualTitle();
            String oldTitle = oldResiduals != null ? oldResiduals.getOtherSpecifiedResidualTitle() : null;

            if (shouldSaveEmptyString(newTitle, oldTitle) ||
                    shouldAddNewTitle(newTitle, oldTitle)) {
                events.add(new SetOtherSpecifiedResidualTitle(newTitle));
            }
        }
    }

    private boolean shouldAddNewTitle(String newTitle, String oldTitle) {
        return newTitle != null &&
                !newTitle.isEmpty() &&
                (oldTitle == null || !oldTitle.equals(newTitle));
    }

    private boolean shouldSaveEmptyString(String newTitle, String oldTitle) {
        return oldTitle != null &&
                newTitle != null &&
                newTitle.isEmpty() &&
                !oldTitle.equals(newTitle);
    }

    private boolean isDerived(LinearizationSpecification specification, List<LinearizationDefinition> linearizationDefinitions) {
        LinearizationDefinition definition = linearizationDefinitions.stream()
                .filter(def -> def.getLinearizationUri().equals(specification.getLinearizationView().toString()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Couldn't find linearization definition for " +specification.getLinearizationView().toString()));

        return definition.getCoreLinId() != null;
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
