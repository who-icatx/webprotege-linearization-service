package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class LinearizationEventsProcessorServiceImpl implements LinearizationEventsProcessorService {
    public WhoficEntityLinearizationSpecification processHistory(@Nonnull EntityLinearizationHistory linearizationHistory) {
        return processHistory(linearizationHistory.getLinearizationRevisions(), linearizationHistory.getWhoficEntityIri());
    }

    public WhoficEntityLinearizationSpecification processHistory(@Nonnull Set<LinearizationRevision> linearizationRevisions, String entityIri) {
        Map<String, Queue<LinearizationSpecificationEvent>> allEventsThatHappenedPerIRI = new HashMap<>();
        List<LinearizationEvent> linearizationResidualEvents = new ArrayList<>();
        List<LinearizationSpecification> linearizationSpecifications = new ArrayList<>();
        /*
        ToDo:
            Investigate to see if it's possible if we have this service scaled horizontally and two users create a new revision at the same time, one on each service.
            Can we have two revisions with the exact same timestamp?
            Will we have this service scaled horizontally?
         */
        linearizationRevisions
                .forEach(linearizationRevision ->
                        linearizationRevision.linearizationEvents()
                                .forEach(event -> {
                                            if (event instanceof LinearizationSpecificationEvent currentEvent) {
                                                Queue<LinearizationSpecificationEvent> eventQueue = allEventsThatHappenedPerIRI.get(currentEvent.getLinearizationView());
                                                if (eventQueue == null) {
                                                    eventQueue = new ConcurrentLinkedQueue<>();
                                                }
                                                eventQueue.add(currentEvent);
                                                allEventsThatHappenedPerIRI.put(currentEvent.getLinearizationView(), eventQueue);
                                            } else {
                                                linearizationResidualEvents.add(event);
                                            }
                                        }
                                )
                );
        allEventsThatHappenedPerIRI.forEach(
                (viewIRI, specQueue) -> {
                    LinearizationSpecification response = new LinearizationSpecification(null,
                            null,
                            null,
                            null,
                            IRI.create(viewIRI),
                            null);
                    while (isNotEmpty(specQueue)) {
                        LinearizationSpecificationEvent event = specQueue.remove();
                        response = (LinearizationSpecification) event.applyEvent(response);
                    }
                    linearizationSpecifications.add(response);
                }
        );
        LinearizationResiduals residuals = new LinearizationResiduals(null,null,null, null);
        for (LinearizationEvent event : linearizationResidualEvents) {
            residuals = (LinearizationResiduals) event.applyEvent(residuals);
        }
        return new WhoficEntityLinearizationSpecification(IRI.create(entityIri),
                residuals,
                linearizationSpecifications);
    }
}
