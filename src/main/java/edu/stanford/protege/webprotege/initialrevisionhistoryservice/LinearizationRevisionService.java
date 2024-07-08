package edu.stanford.protege.webprotege.initialrevisionhistoryservice;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.events.*;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationRevision;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.PROJECT_ID;
import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.EntityLinearizationHistory.WHOFIC_ENTITY_IRI_KEY;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class LinearizationService {

    private final LinearizationRepository linearizationRepository;

    private final MongoTemplate mongoTemplate;

    public LinearizationService(LinearizationRepository linearizationRepository, MongoTemplate mongoTemplate) {
        this.linearizationRepository = linearizationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void addNewRevision(WhoficEntityLinearizationSpecification linearizationSpecification , ProjectId projectId, String userId) {
        var linearizationEvents = mapLinearizationSpecificationsToEvents(linearizationSpecification);
        var existingHistory = getExistingHistory(linearizationSpecification.entityIRI(), projectId);

        if(existingHistory == null) {
            var linearizationRevision = new LinearizationRevision(new Date().getTime(), userId, linearizationEvents);
            existingHistory = new EntityLinearizationHistory(linearizationSpecification.entityIRI(), projectId, new HashSet<>(List.of(linearizationRevision)));
        } else {
            existingHistory.getLinearizationRevisions().add(new LinearizationRevision(new Date().getTime(), userId, linearizationEvents));
        }

        mongoTemplate.save(existingHistory);
    }

    private EntityLinearizationHistory getExistingHistory(IRI entityIri, ProjectId projectId) {
        var query = query(where(WHOFIC_ENTITY_IRI_KEY).is(entityIri.toString()).and(PROJECT_ID).is(projectId));
        return mongoTemplate.findOne(query, EntityLinearizationHistory.class);
    }

    @NotNull
    private Set<LinearizationEvent> mapLinearizationSpecificationsToEvents(WhoficEntityLinearizationSpecification linearizationSpecification) {
        return linearizationSpecification.linearizationSpecifications().stream()
                .map(specification -> {
                    if (specification.getIsIncludedInLinearization() != null) {
                        return new SetIncludedInLinearization(specification.getIsIncludedInLinearization());
                    }
                    if (specification.getIsAuxiliaryAxisChild() != null) {
                        return new SetAuxiliaryAxisChild(specification.getIsAuxiliaryAxisChild());
                    }
                    if (specification.getLinearizationParent() != null) {
                        return new SetLinearizationParent(specification.getLinearizationParent());
                    }
                    if (specification.getLinearizationView() != null) {
                        return new SetLinearizationView(specification.getLinearizationView());
                    }
                    throw new RuntimeException("Everything is null");
                }).collect(Collectors.toSet());
    }

}
