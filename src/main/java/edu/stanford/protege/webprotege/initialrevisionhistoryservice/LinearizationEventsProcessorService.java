package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.isNotEmpty;

@Service
public class LinearizationEventsProcessorService {


    public LinearizationEventsProcessorService() {
    }

    public WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory) {


        Map<IRI, Queue<LinearizationSpecificationEvent>> allEventsThatHappenedPerIRI = new HashMap<>();
        Map<IRI, List<LinearizationSpecificationEvent>> mapOfViewsWithSpecList = new HashMap<>();
        List<LinearizationEvent> linearizationResidualEvents = new ArrayList<>();


        List<LinearizationSpecification> linearizationSpecifications = new ArrayList<>();
        /*
        ToDo:
            Investigate to see if it's possible if we have this service scaled horizontally and two users create a new revision at the same time, one on each service.
            Can we have two revisions with the exact same timestamp?
            Will we have this service scaled horizontally?
         */

//        linearizationHistory.getLinearizationRevisions()
//                .forEach(linearizationRevision ->
//                        linearizationRevision.linearizationEvents()
//                                .forEach(event -> {
//                                    if (event instanceof LinearizationSpecificationEvent currentEvent) {
//                                        var aLinearizationView = mapOfViewsWithSpecList.get(currentEvent.getLinearizationView());
//                                        if (aLinearizationView != null) {
//                                            aLinearizationView.add(currentEvent);
//                                        } else {
//                                            mapOfViewsWithSpecList.put(currentEvent.getLinearizationView(), List.of(currentEvent));
//                                        }
//                                    } else {
//                                        linearizationResidualEvents.add(event);
//                                    }
//                                }));
//
//
//        linearizationSpecifications = mapEventsToSpecifications(mapOfViewsWithSpecList);

        linearizationHistory.getLinearizationRevisions()
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
                    LinearizationSpecification response = new LinearizationSpecification(null, null, null, null, viewIRI, null);

                    while (isNotEmpty(specQueue)) {
                        LinearizationSpecificationEvent event = specQueue.remove();
                        response = (LinearizationSpecification) event.applyEvent(response);
                    }
                    linearizationSpecifications.add(response);
                }
        );


        LinearizationResiduals residuals = new LinearizationResiduals(null, null);
        for (LinearizationEvent event : linearizationResidualEvents) {
            residuals = (LinearizationResiduals) event.applyEvent(residuals);
        }


        return new WhoficEntityLinearizationSpecification(linearizationHistory.getWhoficEntityIri(),
                residuals,
                linearizationSpecifications);
    }
}
