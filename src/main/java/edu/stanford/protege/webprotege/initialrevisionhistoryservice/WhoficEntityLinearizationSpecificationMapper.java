package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WhoficEntityLinearizationSpecificationMapper {

    public WhoficEntityLinearizationSpecification mapToWhoficEntityLinearizationSpecification(EntityLinearizationHistory history) {
        LinearizationResiduals residuals = extractResidualsFromRevisions(history.getLinearizationRevisions());
        List<LinearizationSpecification> specifications = extractSpecificationsFromRevisions(history.getLinearizationRevisions());

        return new WhoficEntityLinearizationSpecification(
                IRI.create(history.getWhoficEntityIri()),
                residuals,
                specifications
        );
    }

    private LinearizationResiduals extractResidualsFromRevisions(Set<LinearizationRevision> revisions) {
        ThreeStateBoolean suppressSpecifiedResidual = null;
        String unspecifiedResidualTitle = null;

        for (LinearizationRevision revision : revisions) {
            for (LinearizationEvent event : revision.linearizationEvents()) {
                if (event instanceof SetSuppressedSpecifiedResidual) {
                    suppressSpecifiedResidual = ThreeStateBoolean.valueOf(event.getValue());
                } else if (event instanceof SetUnspecifiedResidualTitle) {
                    unspecifiedResidualTitle = event.getValue();
                }
            }
        }
        return new LinearizationResiduals(suppressSpecifiedResidual, unspecifiedResidualTitle);
    }

    private List<LinearizationSpecification> extractSpecificationsFromRevisions(Set<LinearizationRevision> revisions) {
        Map<IRI, LinearizationSpecificationBuilder> builderMap = new HashMap<>();

        for (LinearizationRevision revision : revisions) {
            for (LinearizationEvent event : revision.linearizationEvents()) {
                if (event instanceof LinearizationSpecificationEvent specEvent) {
                    IRI linearizationView = IRI.create(specEvent.getLinearizationView());
                    builderMap.putIfAbsent(linearizationView, new LinearizationSpecificationBuilder(linearizationView));
                    builderMap.get(linearizationView).addEvent(specEvent);
                }
            }
        }

        return builderMap.values().stream()
                .map(LinearizationSpecificationBuilder::build)
                .collect(Collectors.toList());
    }

    private static class LinearizationSpecificationBuilder {
        private final IRI linearizationView;
        private ThreeStateBoolean isAuxiliaryAxisChild;
        private ThreeStateBoolean isGrouping;
        private ThreeStateBoolean isIncludedInLinearization;
        private IRI linearizationParent;
        private String codingNote;

        public LinearizationSpecificationBuilder(IRI linearizationView) {
            this.linearizationView = linearizationView;
        }

        public void addEvent(LinearizationSpecificationEvent event) {
            if (event instanceof SetAuxiliaryAxisChild) {
                this.isAuxiliaryAxisChild = ThreeStateBoolean.valueOf(event.getValue());
            } else if (event instanceof SetGrouping) {
                this.isGrouping = ThreeStateBoolean.valueOf(event.getValue());
            } else if (event instanceof SetIncludedInLinearization) {
                this.isIncludedInLinearization = ThreeStateBoolean.valueOf(event.getValue());
            } else if (event instanceof SetLinearizationParent) {
                this.linearizationParent = IRI.create(event.getValue());
            } else if (event instanceof SetCodingNote) {
                this.codingNote = event.getValue();
            }
        }

        public LinearizationSpecification build() {
            return new LinearizationSpecification(isAuxiliaryAxisChild, isGrouping, isIncludedInLinearization, linearizationParent, linearizationView, codingNote);
        }
    }

    public WhoficEntityLinearizationSpecification mapToDefaultWhoficEntityLinearizationSpecification(EntityLinearizationHistory history) {
        LinearizationResiduals residuals = getDefaultResiduals();
        List<LinearizationSpecification> specifications = extractSpecificationsFromRevisions(history.getLinearizationRevisions());

        return new WhoficEntityLinearizationSpecification(
                IRI.create(history.getWhoficEntityIri()),
                residuals,
                specifications
        );
    }

    private LinearizationResiduals getDefaultResiduals() {
        ThreeStateBoolean suppressSpecifiedResidual = ThreeStateBoolean.UNKNOWN;
        String unspecifiedResidualTitle = "";

        return new LinearizationResiduals(suppressSpecifiedResidual, unspecifiedResidualTitle);
    }

    private List<LinearizationSpecification> extractDefaultSpecificationsFromRevisions(Set<LinearizationRevision> revisions) {
        Map<IRI, DefaultLinearizationSpecificationBuilder> builderMap = new HashMap<>();

        for (LinearizationRevision revision : revisions) {
            for (LinearizationEvent event : revision.linearizationEvents()) {
                if (event instanceof LinearizationSpecificationEvent specEvent) {
                    IRI linearizationView = IRI.create(specEvent.getLinearizationView());
                    builderMap.putIfAbsent(linearizationView, new DefaultLinearizationSpecificationBuilder(linearizationView));
                }
            }
        }

        return builderMap.values().stream()
                .map(DefaultLinearizationSpecificationBuilder::build)
                .collect(Collectors.toList());
    }


    private static class DefaultLinearizationSpecificationBuilder {
        private final IRI linearizationView;
        private final ThreeStateBoolean isAuxiliaryAxisChild = ThreeStateBoolean.UNKNOWN;
        private final ThreeStateBoolean isGrouping = ThreeStateBoolean.UNKNOWN;
        private final ThreeStateBoolean isIncludedInLinearization = ThreeStateBoolean.FALSE;
        private final IRI linearizationParent = IRI.create("");
        private final String codingNote = "";

        private DefaultLinearizationSpecificationBuilder(IRI linearizationView) {
            this.linearizationView = linearizationView;
        }

        public LinearizationSpecification build() {
            return new LinearizationSpecification(isAuxiliaryAxisChild, isGrouping, isIncludedInLinearization, linearizationParent, linearizationView, codingNote);
        }
    }
}
