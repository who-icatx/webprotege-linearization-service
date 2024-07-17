package edu.stanford.protege.webprotege.initialrevisionhistoryservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.LinearizationDefinition;

import java.util.List;

public record LinearizationDefinitionResponse(@JsonProperty("definitionList") List<LinearizationDefinition> definitionList) implements Response {

}
