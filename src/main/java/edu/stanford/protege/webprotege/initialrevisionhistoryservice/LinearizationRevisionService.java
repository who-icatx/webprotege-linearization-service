package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.LinearizationEvent;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.Utils.*;

@Service
public class LinearizationRevisionService {

    private final LinearizationRevisionRepository linearizationRevisionRepo;

    private final ObjectMapper objectMapper;

    public LinearizationRevisionService(LinearizationRevisionRepository linearizationRevisionRepo, ObjectMapper objectMapper) {
        this.linearizationRevisionRepo = linearizationRevisionRepo;
        this.objectMapper = objectMapper;
    }

    public EntityLinearizationHistory addNewRevisionToExistingHistory(WhoficEntityLinearizationSpecification linearizationSpecification,
                                                                      EntityLinearizationHistory existingHistory,
                                                                      String userId) {

        Set<LinearizationEvent> linearizationEvents = mapLinearizationSpecificationsToEvents(linearizationSpecification);
        linearizationEvents.addAll(mapLinearizationResidualsEvents(linearizationSpecification));

        existingHistory.getLinearizationRevisions().add(new LinearizationRevision(new Date().getTime(), userId, linearizationEvents));

        return existingHistory;
    }

    public EntityLinearizationHistory addNewRevisionToNewHistory(WhoficEntityLinearizationSpecification linearizationSpecification,
                                                                 ProjectId projectId,
                                                                 String userId) {

        var linearizationEvents = mapLinearizationSpecificationsToEvents(linearizationSpecification);
        linearizationEvents.addAll(mapLinearizationSpecificationsToEvents(linearizationSpecification));

        var linearizationRevision = new LinearizationRevision(new Date().getTime(), userId, linearizationEvents);

        return new EntityLinearizationHistory(linearizationSpecification.entityIRI(), projectId, new HashSet<>(List.of(linearizationRevision)));
    }

    public EntityLinearizationHistory getExistingHistoryOrderedByRevision(String entityIri, ProjectId projectId) {

        EntityLinearizationHistory history = linearizationRevisionRepo.getExistingHistory(entityIri, projectId);

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

    public void saveEntityLinearizationHistory(Set<EntityLinearizationHistory> historiesToBeSaved) {
        var documents = historiesToBeSaved.stream()
                .map(history -> new InsertOneModel<>(objectMapper.convertValue(history, Document.class)))
                .toList();
        linearizationRevisionRepo.bulkWriteDocuments(documents);
    }
}
