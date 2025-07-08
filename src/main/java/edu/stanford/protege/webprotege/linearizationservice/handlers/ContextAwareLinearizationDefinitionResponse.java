package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.linearizationservice.model.LinearizationDefinition;

import java.util.List;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.ContextAwareLinearizationDefinitionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record ContextAwareLinearizationDefinitionResponse(
        @JsonProperty("definitionList") List<LinearizationDefinition> definitionList
) implements Response {
}
