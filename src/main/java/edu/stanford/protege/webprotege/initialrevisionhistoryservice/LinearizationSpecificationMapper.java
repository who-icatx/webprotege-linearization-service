package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;

import java.util.*;
import java.util.stream.Collectors;

public class LinearizationSpecificationMapper implements EventChangeVisitor {
    private ThreeStateBoolean isAuxiliaryAxisChild;
    private ThreeStateBoolean isGrouping;
    private ThreeStateBoolean isIncludedInLinearization;
    private IRI linearizationParent;
    private IRI linearizationView;
    private String codingNote;

    public LinearizationSpecificationMapper() {
    }

    public LinearizationSpecification getLinearizationSpecification() {
        return new LinearizationSpecification(
                isAuxiliaryAxisChild,
                isGrouping,
                isIncludedInLinearization,
                linearizationParent,
                linearizationView,
                codingNote
        );
    }

    @Override
    public void visit(SetAuxiliaryAxisChild event) {
        this.isAuxiliaryAxisChild = ThreeStateBoolean.valueOf(event.getValue());
    }

    @Override
    public void visit(SetCodingNote event) {
        this.codingNote = event.getValue();
    }

    @Override
    public void visit(SetGrouping event) {
        this.isGrouping = ThreeStateBoolean.valueOf(event.getValue());
    }

    @Override
    public void visit(SetIncludedInLinearization event) {
        this.isIncludedInLinearization = ThreeStateBoolean.valueOf(event.getValue());
    }

    @Override
    public void visit(SetLinearizationParent event) {
        this.linearizationParent = IRI.create(event.getValue());
    }

    public static LinearizationSpecification mapEventToSpecification(List<LinearizationSpecificationEvent> events, IRI linearizationView) {
        LinearizationSpecificationMapper mapper = new LinearizationSpecificationMapper();
        mapper.linearizationView = linearizationView;
        for (LinearizationSpecificationEvent event : events) {
            event.accept(mapper);
        }
        return mapper.getLinearizationSpecification();
    }

    public static List<LinearizationSpecification> mapEventsToSpecifications(Map<IRI, List<LinearizationSpecificationEvent>> linearizationEventsMaps) {
        return linearizationEventsMaps.entrySet().stream()
                .map(entry -> mapEventToSpecification(entry.getValue(), entry.getKey()))
                .collect(Collectors.toList());
    }
}
