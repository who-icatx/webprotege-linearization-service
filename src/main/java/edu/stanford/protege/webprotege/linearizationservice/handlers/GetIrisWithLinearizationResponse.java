package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;

import java.util.List;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.GetIrisWithLinearizationRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetIrisWithLinearizationResponse(@JsonProperty("iris") List<String> iris) implements Response {

    @JsonCreator
    public static GetIrisWithLinearizationResponse create(@JsonProperty("iris") List<String> iris) {
        return new GetIrisWithLinearizationResponse(iris);
    }
}
