package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.repositories.history.EntityLinearizationHistoryRepository;
import org.bson.Document;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.*;

@Service
public class LinearizationHistoryService {

    private final ObjectMapper objectMapper;
    private final EntityLinearizationHistoryRepository linearizationHistoryRepository;

    public LinearizationHistoryService(ObjectMapper objectMapper, EntityLinearizationHistoryRepository linearizationHistoryRepository) {
        this.objectMapper = objectMapper;
        this.linearizationHistoryRepository = linearizationHistoryRepository;
    }

    private EntityLinearizationHistory createNewEntityLinearizationHistory(WhoficEntityLinearizationSpecification linearizationSpecification,
                                                                          ProjectId projectId,
                                                                          UserId userId) {

        var linearizationEvents = mapLinearizationSpecificationsToEvents(linearizationSpecification);
        linearizationEvents.addAll(mapLinearizationSpecificationsToEvents(linearizationSpecification));

        var linearizationRevision = LinearizationRevision.create(userId, linearizationEvents);

        return new EntityLinearizationHistory(linearizationSpecification.entityIRI(), projectId, new HashSet<>(List.of(linearizationRevision)));
    }

    public void saveMultipleEntityLinearizationHistories(Set<EntityLinearizationHistory> historiesToBeSaved) {
        var documents = historiesToBeSaved.stream()
                .map(history -> new InsertOneModel<>(objectMapper.convertValue(history, Document.class)))
                .toList();
        linearizationHistoryRepository.bulkWriteDocuments(documents);
    }

    public EntityLinearizationHistory getExistingHistoryOrderedByRevision(IRI entityIri, ProjectId projectId) {
        EntityLinearizationHistory history = linearizationHistoryRepository.findByWhoficEntityIriAndProjectId(entityIri, projectId);
        if (isNotNull(history)) {
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

    public void addRevision(WhoficEntityLinearizationSpecification linearizationSpecification, ProjectId projectId, UserId userId) {

        var existingHistory = getExistingHistoryOrderedByRevision(linearizationSpecification.entityIRI(),projectId);
        if(isNotNull(existingHistory)){
            Set<LinearizationEvent> linearizationEvents = mapLinearizationSpecificationsToEvents(linearizationSpecification);
            linearizationEvents.addAll(mapLinearizationResidualsEvents(linearizationSpecification));

            var newRevision = LinearizationRevision.create(userId, linearizationEvents);

            linearizationHistoryRepository.addRevision(linearizationSpecification.entityIRI(), projectId, newRevision);
        }else {
            var newHistory = createNewEntityLinearizationHistory(linearizationSpecification,projectId,userId);
            linearizationHistoryRepository.saveLinearizationHistory(newHistory);
        }
    }


    public Consumer<List<WhoficEntityLinearizationSpecification>> createBatchProcessorForSavingPaginatedHistories(ProjectId projectId, UserId userId) {
        return page -> {
            var historiesToBeSaved = page.stream()
                    .map(specification -> createNewEntityLinearizationHistory(specification, projectId, userId))
                    .collect(Collectors.toSet());

            saveMultipleEntityLinearizationHistories(historiesToBeSaved);
        };
    }
}