package edu.stanford.protege.webprotege.linearizationservice.services;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.linearizationservice.mappers.WhoficEntityLinearizationSpecificationMapper;
import edu.stanford.protege.webprotege.linearizationservice.model.*;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class LinearizationHistoryProcessorServiceImpl implements LinearizationHistoryProcessorService {

    private final LinearizationHistoryService linearizationHistoryService;
    private final LinearizationEventsProcessorService eventsProcessorService;
    private final WhoficEntityLinearizationSpecificationMapper whoficSpecMapper;
    private final ReadWriteLockService readWriteLockService;

    public LinearizationHistoryProcessorServiceImpl(LinearizationHistoryService linearizationHistoryService,
                                                    LinearizationEventsProcessorService eventsProcessorService,
                                                    WhoficEntityLinearizationSpecificationMapper whoficSpecMapper,
                                                    ReadWriteLockService readWriteLockService) {
        this.linearizationHistoryService = checkNotNull(linearizationHistoryService);
        this.eventsProcessorService = checkNotNull(eventsProcessorService);
        this.whoficSpecMapper = checkNotNull(whoficSpecMapper);
        this.readWriteLockService = checkNotNull(readWriteLockService);
    }


    @Override
    public Optional<WhoficEntityLinearizationSpecification> mergeLinearizationViewsFromParentsAndGetDefaultSpec(IRI currentEntityIri, Set<IRI> parentEntityIris, ProjectId projectId) {

        return readWriteLockService.executeReadLock(() -> {
            var missingSpecViews = new ArrayList<LinearizationSpecification>();

            WhoficEntityLinearizationSpecification currentSpecs =
                    linearizationHistoryService.getExistingHistoryOrderedByRevision(currentEntityIri, projectId)
                            .map(eventsProcessorService::processHistory)
                            .orElseGet(() -> new WhoficEntityLinearizationSpecification(currentEntityIri, null, Collections.emptyList()));

            parentEntityIris.stream()
                    .flatMap(parentIri -> {
                        var parentEntityHistory = linearizationHistoryService.getExistingHistoryOrderedByRevision(parentIri, projectId);
                        if (parentEntityHistory.isEmpty()) {
                            return Stream.of();
                        }

                        var parentWhoficSpec = eventsProcessorService.processHistory(parentEntityHistory.get());
                        var newDefaultWhoficSpecs = whoficSpecMapper.mapToDefaultWhoficEntityLinearizationSpecification(currentEntityIri, parentWhoficSpec);

                        return newDefaultWhoficSpecs.linearizationSpecifications().stream();
                    }).forEach(parentSpec -> {
                        var viewNotFound = currentSpecs.linearizationSpecifications().stream().noneMatch(currSpec -> currSpec.getLinearizationView().equals(parentSpec.getLinearizationView())) &&
                                missingSpecViews.stream().noneMatch(missingSpecView -> missingSpecView.getLinearizationView().equals(parentSpec.getLinearizationView()));
                        if (viewNotFound) {
                            missingSpecViews.add(parentSpec);
                        }
                    });
            if (missingSpecViews.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new WhoficEntityLinearizationSpecification(currentEntityIri, null, missingSpecViews));
        });
    }
}
