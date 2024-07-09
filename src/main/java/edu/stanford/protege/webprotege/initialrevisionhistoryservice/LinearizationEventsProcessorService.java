package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LinearizationEventsProcessorService {

    private final ObjectFactory<EventChangeVisitorImpl> eventChangeVisitorFactory;

    public LinearizationEventsProcessorService(ObjectFactory<EventChangeVisitorImpl> eventChangeVisitorFactory) {
        this.eventChangeVisitorFactory = eventChangeVisitorFactory;
    }

    public WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory) {

        EventChangeVisitorImpl eventChangeVisitor = eventChangeVisitorFactory.getObject();

        /*
        ToDo:
            Investigate to see if it's possible if we have this service scaled horizontally and two users create a new revision at the same time, one on each service.
            Can we have two revisions with the exact same timestamp?
            Will we have this service scaled horizontally?
         */
        // Sort linearizationRevisions by timestamp in ascending order (oldest first)
        List<LinearizationRevision> sortedRevisions = linearizationHistory.getLinearizationRevisions()
                .stream()
                .sorted(Comparator.comparingLong(LinearizationRevision::timestamp))
                .toList();

        // Process each revision in the sorted order
        sortedRevisions.forEach(linearizationRevision ->
                linearizationRevision.linearizationEvents()
                        .forEach(event -> event.accept(eventChangeVisitor))
        );

        return new WhoficEntityLinearizationSpecification(linearizationHistory.getWhoficEntityIri(),
                eventChangeVisitor.getLinearizationResiduals(),
                eventChangeVisitor.getLinearizationSpecifications());
    }
}
