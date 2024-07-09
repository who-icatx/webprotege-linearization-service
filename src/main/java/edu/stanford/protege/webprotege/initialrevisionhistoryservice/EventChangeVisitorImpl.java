package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;

@Component
@Scope("prototype")
public class EventChangeVisitorImpl implements EventChangeVisitor {

    private LinearizationResiduals linearizationResiduals = new LinearizationResiduals(null, null);

    private final Map<IRI, LinearizationSpecification> linearizationEventsMaps = new HashMap<>();

    @Override
    public void visit(SetUnspecifiedResidualTitle setUnspecifiedResidualTitle) {
        updateLinearizationResiduals(setUnspecifiedResidualTitle);
    }

    @Override
    public void visit(SetSuppressedSpecifiedResidual setSuppressedSpecifiedResidual) {
        updateLinearizationResiduals(setSuppressedSpecifiedResidual);
    }

    @Override
    public void visit(@NotNull SetLinearizationParent setLinearizationParent) {
        updateLinearizationSpecificationEventsMap(setLinearizationParent);
    }

    @Override
    public void visit(@Nonnull SetAuxiliaryAxisChild setAuxiliaryAxisChildChange) {
        updateLinearizationSpecificationEventsMap(setAuxiliaryAxisChildChange);
    }

    @Override
    public void visit(@Nonnull SetCodingNote setCodingNoteChange) {
        updateLinearizationSpecificationEventsMap(setCodingNoteChange);
    }

    @Override
    public void visit(@Nonnull SetGrouping setGroupingChange) {
        updateLinearizationSpecificationEventsMap(setGroupingChange);
    }

    @Override
    public void visit(@Nonnull SetIncludedInLinearization setIncludedInLinearizationChange) {
        updateLinearizationSpecificationEventsMap(setIncludedInLinearizationChange);
    }

    private void updateLinearizationSpecificationEventsMap(LinearizationSpecificationEvent linearizationSpecificationEvent) {
        IRI linearizationView = linearizationSpecificationEvent.getLinearizationView();
        LinearizationSpecification linearizationForView = linearizationEventsMaps.get(linearizationView);
        if (linearizationForView != null) {
            linearizationForView = (LinearizationSpecification) linearizationSpecificationEvent.applyEvent(linearizationForView);
        } else {
            LinearizationSpecification newLinearizationForView = new LinearizationSpecification(
                    null,
                    null,
                    null,
                    null,
                    linearizationView,
                    null);
            newLinearizationForView = (LinearizationSpecification) linearizationSpecificationEvent.applyEvent(newLinearizationForView);
            linearizationEventsMaps.put(linearizationView, newLinearizationForView);
        }
    }

    private void updateLinearizationResiduals(LinearizationEvent linearizationEvent) {
        linearizationResiduals = (LinearizationResiduals) linearizationEvent.applyEvent(linearizationResiduals);
    }

    public LinearizationResiduals getLinearizationResiduals() {
        return linearizationResiduals;
    }

    public List<LinearizationSpecification> getLinearizationSpecifications() {
        return new ArrayList<>(linearizationEventsMaps.values());
    }

    public void reset() {
        this.linearizationResiduals = new LinearizationResiduals(null, null);
        this.linearizationEventsMaps.clear();
    }
}