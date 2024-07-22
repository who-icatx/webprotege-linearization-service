package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record CreateLinearizationFromParentResponse() implements Response {
}
