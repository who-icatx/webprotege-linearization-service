package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class LinearizationEventsProcessorService {

    private final ObjectFactory<EventChangeVisitorImpl> eventChangeVisitorFactory;

    public LinearizationEventsProcessorService(ObjectFactory<EventChangeVisitorImpl> eventChangeVisitorFactory) {
        this.eventChangeVisitorFactory = eventChangeVisitorFactory;
    }

    public WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory) {

        EventChangeVisitorImpl eventChangeVisitor = eventChangeVisitorFactory.getObject();

        Map<IRI, Queue<LinearizationSpecificationEvent>> allEventsThatHappenedPerIRI = new HashMap<>();

        List<LinearizationSpecification> linearizationSpecifications = new ArrayList<>();
        List<LinearizationEvent> otherEvents = new ArrayList<>();
        /*
        ToDo:
            Investigate to see if it's possible if we have this service scaled horizontally and two users create a new revision at the same time, one on each service.
            Can we have two revisions with the exact same timestamp?
            Will we have this service scaled horizontally?
         */

        // Process each revision in the sorted order
        linearizationHistory.getLinearizationRevisions()
                .forEach(linearizationRevision ->
                linearizationRevision.linearizationEvents()
                        .forEach(event -> event.accept(eventChangeVisitor))
        );

        for(LinearizationRevision revision : linearizationHistory.getLinearizationRevisions()){
            for (LinearizationEvent event: revision.linearizationEvents()){
                if(event instanceof LinearizationSpecificationEvent){
                    LinearizationSpecificationEvent currentEvent = (LinearizationSpecificationEvent) event;
                    Queue<LinearizationSpecificationEvent> eventQueue = allEventsThatHappenedPerIRI.get(currentEvent.getLinearizationView());
                    if(eventQueue==null){
                        eventQueue = new ConcurrentLinkedQueue<>();
                    }
                    eventQueue.add(currentEvent);
                    allEventsThatHappenedPerIRI.put(currentEvent.getLinearizationView(),eventQueue);
                }
                else  {
                    otherEvents.add(event);
                }
            }
        }

        for(IRI linearizationView: allEventsThatHappenedPerIRI.keySet()){
            LinearizationSpecification response = new LinearizationSpecification(null, null, null,null, linearizationView, null) ;

            while (!allEventsThatHappenedPerIRI.get(linearizationView).isEmpty()){
                LinearizationSpecificationEvent event = allEventsThatHappenedPerIRI.get(linearizationView).remove();
                response = (LinearizationSpecification) event.applyEvent(response);
            }
            linearizationSpecifications.add(response);
        }


        LinearizationResiduals residuals = new LinearizationResiduals(null, null);
        for(LinearizationEvent event : otherEvents) {
            residuals = (LinearizationResiduals) event.applyEvent(residuals);
        }


        return new WhoficEntityLinearizationSpecification(linearizationHistory.getWhoficEntityIri(),
                    residuals,
               linearizationSpecifications);
    }
}
