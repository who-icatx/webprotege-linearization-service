package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.dispatch.Result;

import java.util.List;

public record GetMatchingCriteriaResponse(@JsonProperty("matchingKeys")List<String> matchingKeys) implements Result {
}
