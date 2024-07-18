package edu.stanford.protege.webprotege.initialrevisionhistoryservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.LinearizationEventMapper;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.LinearizationHistoryRepository;
import org.bson.Document;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class LinearizationHistoryServiceImpl implements LinearizationHistoryService {

    private final ObjectMapper objectMapper;
    private final LinearizationHistoryRepository linearizationHistoryRepository;
    private final LinearizationEventMapper eventMapper;
    private final ReadWriteLockService readWriteLock;


    public LinearizationHistoryServiceImpl(ObjectMapper objectMapper,
                                           LinearizationHistoryRepository linearizationHistoryRepository,
                                           LinearizationEventMapper eventMapper, ReadWriteLockService readWriteLock) {
        this.objectMapper = objectMapper;
        this.linearizationHistoryRepository = linearizationHistoryRepository;
        this.eventMapper = eventMapper;
        this.readWriteLock = readWriteLock;
    }

    private EntityLinearizationHistory createNewEntityLinearizationHistory(WhoficEntityLinearizationSpecification linearizationSpecification,
                                                                           ProjectId projectId,
                                                                           UserId userId) {

        var linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(linearizationSpecification);
        linearizationEvents.addAll(eventMapper.mapLinearizationSpecificationsToEvents(linearizationSpecification));

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
    public EntityLinearizationHistory getExistingHistoryOrderedByRevision(IRI entityIri, ProjectId projectId) {
        EntityLinearizationHistory history = linearizationHistoryRepository.findHistoryByEntityIriAndProjectId(entityIri.toString(), projectId);
        if (history != null) {
            // Sort the linearizationRevisions by timestamp
            Set<LinearizationRevision> sortedRevisions = history.getLinearizationRevisions()
                    .stream()
                    .sorted(Comparator.comparingLong(LinearizationRevision::timestamp))
                    .collect(Collectors.toCollection(TreeSet::new));
            // Return a new EntityLinearizationHistory object with the sorted revisions
            return new EntityLinearizationHistory(history.getWhoficEntityIri(), history.getProjectId(), sortedRevisions);
        }
        return null;
    }

    @Override
    public void addRevision(WhoficEntityLinearizationSpecification linearizationSpecification,
                            ProjectId projectId, UserId userId) {
        readWriteLock.executeWriteLock(() -> {
            var existingHistory = getExistingHistoryOrderedByRevision(linearizationSpecification.entityIRI(), projectId);
            if (existingHistory != null) {
                Set<LinearizationEvent> linearizationEvents = eventMapper.mapLinearizationSpecificationsToEvents(linearizationSpecification);
                linearizationEvents.addAll(eventMapper.mapLinearizationResidualsToEvents(linearizationSpecification));

                var newRevision = LinearizationRevision.create(userId, linearizationEvents);

                linearizationHistoryRepository.addRevision(linearizationSpecification.entityIRI().toString(), projectId, newRevision);
            } else {
                var newHistory = createNewEntityLinearizationHistory(linearizationSpecification, projectId, userId);
                linearizationHistoryRepository.saveLinearizationHistory(newHistory);
            }
        });
    }


    @Override
    public Consumer<List<WhoficEntityLinearizationSpecification>> createBatchProcessorForSavingPaginatedHistories(ProjectId projectId, UserId userId) {
        return page -> {
            var historiesToBeSaved = page.stream()
                    .map(specification -> createNewEntityLinearizationHistory(specification, projectId, userId))
                    .collect(Collectors.toSet());

            saveMultipleEntityLinearizationHistories(historiesToBeSaved);
        };
    }
}