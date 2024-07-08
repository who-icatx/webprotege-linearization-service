package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;
import java.util.*;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.LinearizationSpecificationMapper.mapEventsToSpecifications;


public class LinearizationEventsProcessor {
    public WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory) {

        LinearizationResiduals linearizationResiduals = new LinearizationResiduals();

        Map<IRI, List<LinearizationSpecificationEvent>> linearizationEventsMaps = new HashMap<>();


        EventChangeVisitor eventChangeVisitor = new EventChangeVisitor() {

            @Override
            public void visit(SetUnspecifiedResidualTitle setUnspecifiedResidualTitle) {
                linearizationResiduals.setUnspecifiedResidualTitle(setUnspecifiedResidualTitle.getValue());
            }

            @Override
            public void visit(SetSuppressedSpecifiedResidual setSuppressedSpecifiedResidual) {
                linearizationResiduals.setSuppressSpecifiedResidual(ThreeStateBoolean.valueOf(setSuppressedSpecifiedResidual.getValue()));
            }

            @Override
            public void visit(SetLinearizationParent setLinearizationParent) {
                updateLinearizationSpecificationEventsMap(linearizationEventsMaps, setLinearizationParent);

            }

            @Override
            public void visit(@Nonnull SetAuxiliaryAxisChild setAuxiliaryAxisChildChange) {
                updateLinearizationSpecificationEventsMap(linearizationEventsMaps, setAuxiliaryAxisChildChange);

            }

            @Override
            public void visit(@Nonnull SetCodingNote setCodingNoteChange) {
                updateLinearizationSpecificationEventsMap(linearizationEventsMaps, setCodingNoteChange);
            }

            @Override
            public void visit(@Nonnull SetGrouping setGroupingChange) {
                updateLinearizationSpecificationEventsMap(linearizationEventsMaps, setGroupingChange);
            }

            @Override
            public void visit(@Nonnull SetIncludedInLinearization setIncludedInLinearizationChange) {
                updateLinearizationSpecificationEventsMap(linearizationEventsMaps, setIncludedInLinearizationChange);
            }
        };

        linearizationHistory.linearizationRevisions()
                .forEach(linearizationRevision -> linearizationRevision.linearizationEvents()
                        .forEach(event -> event.accept(eventChangeVisitor))
                );
        return new WhoficEntityLinearizationSpecification(linearizationHistory.whoficEntityIri(),
                linearizationResiduals,
                mapEventsToSpecifications(linearizationEventsMaps));
    }


    private void updateLinearizationSpecificationEventsMap(Map<IRI, List<LinearizationSpecificationEvent>> linearizationEventsMaps, LinearizationSpecificationEvent linearizationSpecificationEvent){
        var linearizationView = linearizationEventsMaps.get(linearizationSpecificationEvent.getLinearizationView());
        if (linearizationView != null) {
            var linearizationExists = linearizationView.stream()
                    .filter(event -> event.getType().equals(linearizationSpecificationEvent.getType()))
                    .findAny();
            linearizationExists.ifPresentOrElse(linearization -> linearization.applyEvent(linearizationSpecificationEvent),
                    () -> linearizationView.add(new SetLinearizationParent(IRI.create(linearizationSpecificationEvent.getValue()), linearizationSpecificationEvent.getLinearizationView())));
        } else {
            var newLinearizationView = new ArrayList<LinearizationSpecificationEvent>();
            newLinearizationView.add(linearizationSpecificationEvent);
            linearizationEventsMaps.put(linearizationSpecificationEvent.getLinearizationView(), newLinearizationView);
        }
    }
}
