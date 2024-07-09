package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Service;

@Service
public class LinearizationEventsProcessorService {

    private final ObjectFactory<EventChangeVisitorImpl> eventChangeVisitorFactory;

    public LinearizationEventsProcessorService(ObjectFactory<EventChangeVisitorImpl> eventChangeVisitorFactory) {
        this.eventChangeVisitorFactory = eventChangeVisitorFactory;
    }

    public WhoficEntityLinearizationSpecification processHistory(EntityLinearizationHistory linearizationHistory) {

        EventChangeVisitorImpl eventChangeVisitor = eventChangeVisitorFactory.getObject();

        linearizationHistory.linearizationRevisions()
                .forEach(linearizationRevision ->
                        linearizationRevision.linearizationEvents()
                                .forEach(event -> event.accept(eventChangeVisitor))
                );

        return new WhoficEntityLinearizationSpecification(linearizationHistory.whoficEntityIri(),
                eventChangeVisitor.getLinearizationResiduals(),
                eventChangeVisitor.getLinearizationSpecifications());
    }
}
