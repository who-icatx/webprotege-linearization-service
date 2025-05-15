package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.linearizationservice.events.*;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class LinearizationEventsProcessorServiceImpl implements LinearizationEventsProcessorService {
    
    private final LinearizationDefinitionService linearizationDefinitionService;

    public LinearizationEventsProcessorServiceImpl(LinearizationDefinitionService linearizationDefinitionService) {
        this.linearizationDefinitionService = linearizationDefinitionService;
    }

    public WhoficEntityLinearizationSpecification processHistory(@Nonnull EntityLinearizationHistory linearizationHistory, ExecutionContext executionContext) {
        return processHistory(linearizationHistory.getLinearizationRevisions(),ProjectId.valueOf(linearizationHistory.getProjectId()), executionContext, linearizationHistory.getWhoficEntityIri());
    }

    public WhoficEntityLinearizationSpecification processHistory(@Nonnull Set<LinearizationRevision> linearizationRevisions,
                                                                 ProjectId projectId,
                                                                 ExecutionContext executionContext,
                                                                 String entityIri) {
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
        LinearizationResiduals residuals = new LinearizationResiduals(null, null, null, null);
        for (LinearizationEvent event : linearizationResidualEvents) {
            residuals = (LinearizationResiduals) event.applyEvent(residuals);
        }

        // Filter specifications based on user access rights
        List<LinearizationSpecification> accessibleSpecifications = filterAccessibleSpecifications(
            linearizationSpecifications,
            executionContext,
            projectId,
            entityIri
        );

        return new WhoficEntityLinearizationSpecification(IRI.create(entityIri),
                residuals,
                accessibleSpecifications);
    }

    private List<LinearizationSpecification> filterAccessibleSpecifications(
            List<LinearizationSpecification> specifications,
            ExecutionContext executionContext,
            ProjectId projectId,
            String entityIri) {
        try {
            // Get user's accessible linearizations
            LinearizationDefinitionService.AllowedLinearizationDefinitions allowedLinearizations = 
                linearizationDefinitionService.getUserAccessibleLinearizations(
                    projectId, 
                    IRI.create(entityIri), 
                    executionContext
                );

            // Filter specifications based on readable linearizations
            return specifications.stream()
                .filter(spec -> allowedLinearizations.readableLinearizations()
                    .contains(spec.getLinearizationView().toString()))
                .collect(Collectors.toList());

        } catch (Exception e) {
            // Log error and return empty list or throw appropriate exception
            throw new RuntimeException("Failed to filter accessible linearizations", e);
        }
    }
}
