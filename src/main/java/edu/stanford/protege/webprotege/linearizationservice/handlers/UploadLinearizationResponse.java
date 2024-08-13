package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotege.linearizationservice.handlers.UploadLinearizationRequest.CHANNEL;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@JsonTypeName(CHANNEL)
public record UploadLinearizationResponse() implements Response {

    public static UploadLinearizationResponse create() {
        return new UploadLinearizationResponse();
    }

}
