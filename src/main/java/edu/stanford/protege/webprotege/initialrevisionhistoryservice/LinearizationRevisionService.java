package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.*;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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


    private Set<LinearizationEvent> mapLinearizationResidualsEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        Set<LinearizationEvent> residuals = new HashSet<>();

        if (linearizationSpecification.linearizationResiduals() != null) {
            if (linearizationSpecification.linearizationResiduals().getSuppressSpecifiedResidual() != null) {
                residuals.add(new SetSuppressedSpecifiedResidual(linearizationSpecification.linearizationResiduals().getSuppressSpecifiedResidual()));
            }
            if (linearizationSpecification.linearizationResiduals().getUnspecifiedResidualTitle() != null) {
                residuals.add(new SetUnspecifiedResidualTitle(linearizationSpecification.linearizationResiduals().getUnspecifiedResidualTitle()));
            }
        }
        return residuals;
    }

    @NotNull
    private Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        return linearizationSpecification.linearizationSpecifications().stream()
                .flatMap(specification -> {
                    List<LinearizationSpecificationEvent> response = new ArrayList<>();

                    if (specification.getIsIncludedInLinearization() != null) {
                        response.add(new SetIncludedInLinearization(specification.getIsIncludedInLinearization(), specification.getLinearizationView()));
                    }
                    if (specification.getIsAuxiliaryAxisChild() != null) {
                        response.add(new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild(), specification.getLinearizationView()));
                    }
                    if (specification.getLinearizationParent() != null) {
                        response.add(new SetLinearizationParent(specification.getLinearizationParent(), specification.getLinearizationView()));
                    }
                    if (specification.getIsGrouping() != null) {
                        response.add(new SetGrouping(specification.getIsGrouping(), specification.getLinearizationView()));
                    }
                    if (specification.getCodingNote() != null) {
                        response.add(new SetCodingNote(specification.getCodingNote(), specification.getLinearizationView()));
                    }

                    return response.stream();
                }).collect(Collectors.toSet());
    }

    public void saveEntityLinearizationHistory(Set<EntityLinearizationHistory> historiesToBeSaved) {
        var documents = historiesToBeSaved.stream()
                .map(history -> new InsertOneModel<>(objectMapper.convertValue(history, Document.class)))
                .toList();
        linearizationRevisionRepo.bulkWriteDocuments(documents);
    }
}
