package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.UploadLinearizationRequest.CHANNEL;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@JsonTypeName(CHANNEL)
public record UploadLinearizationRequest(@JsonProperty("documentLocation") String documentLocation, @JsonProperty("projectId")
                                         ProjectId projectId) implements Request<UploadLinearizationResponse> {

    public static final String CHANNEL = "webprotege.linearization.ProcessUploadedLinearization";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
