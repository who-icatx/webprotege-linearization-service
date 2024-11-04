package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

import java.util.List;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.CreateLinearizationFromParentRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetIrisWithLinearizationRequest(
        @JsonProperty("iris") List<String> iris,
        @JsonProperty("projectId") ProjectId projectId
) implements Request<GetIrisWithLinearizationResponse> {

    public final static String CHANNEL = "webprotege.linearization.GetIrisWithLinearization";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
