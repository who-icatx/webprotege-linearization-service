package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.initialrevisionhistoryservice.model.WhoficEntityLinearizationSpecification;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.GetEntityLinearizationsRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityLinearizationsResponse(@JsonProperty("entityIri")
                                              String entityIri,
                                              @JsonProperty("linearizationSpecification")
                                              WhoficEntityLinearizationSpecification linearizationSpecification) implements Response {
}