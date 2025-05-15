package edu.stanford.protege.webprotege.linearizationservice.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaRequest;
import edu.stanford.protege.webprotege.linearizationservice.handlers.GetMatchingCriteriaResponse;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationRowsCapability;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class LinearizationDefinitionService {
    private final CommandExecutor<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> getAuthorizedActionsExecutor;

    private final CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor;

    private final ObjectMapper objectMapper;

    public LinearizationDefinitionService(CommandExecutor<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> getAuthorizedActionsExecutor, CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor, ObjectMapper objectMapper) {
        this.getAuthorizedActionsExecutor = getAuthorizedActionsExecutor;
        this.getMatchingCriteriaExecutor = getMatchingCriteriaExecutor;
        this.objectMapper = objectMapper;
    }


    public AllowedLinearizationDefinitions getUserAccessibleLinearizations(ProjectId projectId, IRI entityIri, ExecutionContext executionContext) throws ExecutionException, InterruptedException {

        GetAuthorizedCapabilitiesResponse authorizedResponse = getAuthorizedActionsExecutor.execute(new GetAuthorizedCapabilitiesRequest(
                ProjectResource.forProject(projectId),
                Subject.forUser(executionContext.userId())), executionContext).get();

        Map<String, List<CompositeRootCriteria>> criteriaMap = new HashMap<>();

        for (Capability capability : authorizedResponse.capabilities()) {
            if (capability.asGenericCapability().type().equals(LinearizationRowsCapability.TYPE)) {
                LinearizationRowsCapability linearizationCapability = objectMapper.convertValue(capability, LinearizationRowsCapability.class);
                List<CompositeRootCriteria> existingCriteria = criteriaMap.get(linearizationCapability.id());
                if (existingCriteria == null) {
                    existingCriteria = new ArrayList<>();
                }
                if(linearizationCapability.contextCriteria() != null) {
                    existingCriteria.add(linearizationCapability.contextCriteria());
                }
                criteriaMap.put(linearizationCapability.id(), existingCriteria);
            }
        }

        GetMatchingCriteriaResponse response = getMatchingCriteriaExecutor.execute(new GetMatchingCriteriaRequest(criteriaMap, projectId, entityIri), executionContext).get();

        Set<String> editableLinearizations = new HashSet<>();
        Set<String> readableLinearizations = new HashSet<>();


        for (Capability capability : authorizedResponse.capabilities()) {
            if (capability.asGenericCapability().type().equals(LinearizationRowsCapability.TYPE)) {
                LinearizationRowsCapability linearizationCapability = objectMapper.convertValue(capability, LinearizationRowsCapability.class);

                if ( (response.matchingKeys().contains(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW) ||
                        criteriaMap.get(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW).isEmpty()
                        ) &&
                        linearizationCapability.id().equals(LinearizationRowsCapability.EDIT_LINEARIZATION_ROW)) {
                    editableLinearizations.addAll(linearizationCapability.linearizationIds());
                    readableLinearizations.addAll(linearizationCapability.linearizationIds());
                }
                if ( (response.matchingKeys().contains(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)||
                        criteriaMap.get(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW).isEmpty()) &&
                        linearizationCapability.id().equals(LinearizationRowsCapability.VIEW_LINEARIZATION_ROW)) {
                    readableLinearizations.addAll(linearizationCapability.linearizationIds());
                }
            }
        }

        return new AllowedLinearizationDefinitions(new ArrayList<>(readableLinearizations), new ArrayList<>(editableLinearizations));
    }


    public record AllowedLinearizationDefinitions(List<String> readableLinearizations,
                                           List<String> editableLinearizations) {
    }
}
