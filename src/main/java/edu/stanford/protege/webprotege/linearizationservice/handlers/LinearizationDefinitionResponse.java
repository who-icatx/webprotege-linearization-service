package edu.stanford.protege.webprotege.liniarizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.liniarizationservice.model.LinearizationDefinition;

import java.util.List;

import static edu.stanford.protege.webprotege.liniarizationservice.handlers.LinearizationDefinitionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record LinearizationDefinitionResponse(
        @JsonProperty("definitionList") List<LinearizationDefinition> definitionList) implements Response {

}
