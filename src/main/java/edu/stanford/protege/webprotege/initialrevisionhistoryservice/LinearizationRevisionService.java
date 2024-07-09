package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.InsertOneModel;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.HistoryId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class LinearizationRevisionService {

    public final static String REVISION_HISTORY_COLLECTION = "EntityLinearizationHistories";

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    public LinearizationRevisionService(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
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

        return new EntityLinearizationHistory(new HistoryId("668d009620d6a71e9c8f762e"), linearizationSpecification.entityIRI(), projectId, new HashSet<>(List.of(linearizationRevision)));
    }


    public EntityLinearizationHistory getExistingHistory(String entityIri, ProjectId projectId) {
        var query = query(where(WHOFIC_ENTITY_IRI_KEY).is(entityIri).and(PROJECT_ID).is(projectId).in(REVISION_HISTORY_COLLECTION));
        EntityLinearizationHistory history = mongoTemplate.findOne(query, EntityLinearizationHistory.class);

        /*
        ToDo:
            make test for this
         */
        if (history != null) {
            // Sort the linearizationRevisions by timestamp
            Set<LinearizationRevision> sortedRevisions = history.linearizationRevisions()
                    .stream()
                    .sorted(Comparator.comparingLong(LinearizationRevision::timestamp))
                    .collect(Collectors.toCollection(TreeSet::new));

            // Return a new EntityLinearizationHistory object with the sorted revisions
            return new EntityLinearizationHistory(history.whoficEntityIri(), history.projectId(), sortedRevisions);
        }

        return null;
    }

    public void saveLinearizationHistory(EntityLinearizationHistory entityLinearizationHistory) {
        mongoTemplate.save(entityLinearizationHistory, REVISION_HISTORY_COLLECTION);
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

    public void saveAll(Set<EntityLinearizationHistory> historiesToBeSaved) {
        var collection = mongoTemplate.getCollection(REVISION_HISTORY_COLLECTION);
        /* THIS WORKS IF THE RECEIVED HISTORIES ARE UNIQUE */
        var documents = historiesToBeSaved.stream()
                .map(history -> new InsertOneModel<>(objectMapper.convertValue(history, Document.class)))
                .collect(Collectors.toList());

        collection.bulkWrite(documents);
    }
}
