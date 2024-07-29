package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.mappers.LinearizationEventMapper;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.LinearizationHistoryRepository;
import org.bson.Document;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Service
public class LinearizationHistoryServiceImpl implements LinearizationHistoryService {

    private final ObjectMapper objectMapper;
    private final LinearizationHistoryRepository linearizationHistoryRepository;
    private final LinearizationEventMapper eventMapper;
    private final ReadWriteLockService readWriteLock;

    private final LinearizationEventsProcessorService processorService;

    public LinearizationHistoryServiceImpl(ObjectMapper objectMapper,
                                           LinearizationHistoryRepository linearizationHistoryRepository,
                                           LinearizationEventMapper eventMapper, ReadWriteLockService readWriteLock, LinearizationEventsProcessorService processorService) {
        this.objectMapper = objectMapper;
        this.linearizationHistoryRepository = linearizationHistoryRepository;
        this.eventMapper = eventMapper;
        this.readWriteLock = readWriteLock;
        this.processorService = processorService;
    }

    private EntityLinearizationHistory createNewEntityLinearizationHistory(WhoficEntityLinearizationSpecification linearizationSpecification,
                                                                           ProjectId projectId,
                                                                           UserId userId) {

        var linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(linearizationSpecification);
        linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(linearizationSpecification));

        if (linearizationEvents.isEmpty()) {
            throw new RuntimeException("Trying to create history with no events! EntityIri: " + linearizationSpecification.entityIRI());
        }

        var linearizationRevision = LinearizationRevision.create(userId, linearizationEvents);

        return new EntityLinearizationHistory(linearizationSpecification.entityIRI().toString(), projectId.id(), new HashSet<>(List.of(linearizationRevision)));
    }

    @Override
    public void saveMultipleEntityLinearizationHistories(Set<EntityLinearizationHistory> historiesToBeSaved) {
        var documents = historiesToBeSaved.stream()
                .map(history -> new InsertOneModel<>(objectMapper.convertValue(history, Document.class)))
                .toList();
        linearizationHistoryRepository.bulkWriteDocuments(documents);
    }

    @Override
    public Optional<EntityLinearizationHistory> getExistingHistoryOrderedByRevision(IRI entityIri, ProjectId projectId) {
        return linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(entityIri.toString(), projectId)
                .map(history -> {
                    Set<LinearizationRevision> sortedRevisions = history.getLinearizationRevisions()
                            .stream()
                            .sorted(Comparator.comparingLong(LinearizationRevision::timestamp))
                            .collect(Collectors.toCollection(TreeSet::new));
                    // Return a new EntityLinearizationHistory object with the sorted revisions
                    return new EntityLinearizationHistory(history.getWhoficEntityIri(), history.getProjectId(), sortedRevisions);
                });

    }

    @Override
    public void addRevision(WhoficEntityLinearizationSpecification linearizationSpecification,
                            ProjectId projectId, UserId userId) {
        readWriteLock.executeWriteLock(() -> {
                    var existingHistoryOptional = getExistingHistoryOrderedByRevision(linearizationSpecification.entityIRI(), projectId);
                    existingHistoryOptional.ifPresentOrElse(history -> {

                                WhoficEntityLinearizationSpecification oldSpec = processorService.processHistory(existingHistoryOptional.get());

                                Set<LinearizationEvent> linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(linearizationSpecification, oldSpec);

                                linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(linearizationSpecification, oldSpec));

                                if(!linearizationEvents.isEmpty()) {
                                    var newRevision = LinearizationRevision.create(userId, linearizationEvents);

                                    linearizationHistoryRepository.addRevision(linearizationSpecification.entityIRI().toString(), projectId, newRevision);
                                }
                            }, () -> {
                                var newHistory = createNewEntityLinearizationHistory(linearizationSpecification, projectId, userId);
                                linearizationHistoryRepository.saveLinearizationHistory(newHistory);
                            }
                    );
                }
        );
    }

    @Override
    public Consumer<List<WhoficEntityLinearizationSpecification>> createBatchProcessorForSavingPaginatedHistories(ProjectId projectId, UserId userId) {
        return page -> {
            if (isNotEmpty(page)) {
                var historiesToBeSaved = page.stream()
                        .map(specification -> createNewEntityLinearizationHistory(specification, projectId, userId))
                        .collect(Collectors.toSet());

                saveMultipleEntityLinearizationHistories(historiesToBeSaved);
            }
        };
    }
}
