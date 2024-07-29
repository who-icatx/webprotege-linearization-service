package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationDefinition;

import java.util.List;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.LinearizationDefinitionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record LinearizationDefinitionResponse(
        @JsonProperty("definitionList") List<LinearizationDefinition> definitionList) implements Response {

}
