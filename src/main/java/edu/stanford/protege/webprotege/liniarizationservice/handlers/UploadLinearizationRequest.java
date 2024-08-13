package edu.stanford.protege.webprotege.liniarizationservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.change.OntologyDocumentId;
import edu.stanford.protege.webprotege.common.*;

import static edu.stanford.protege.webprotege.liniarizationservice.handlers.UploadLinearizationRequest.CHANNEL;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-05-03
 */
@JsonTypeName(CHANNEL)
public record UploadLinearizationRequest(@JsonProperty("documentId") OntologyDocumentId documentId,
                                         @JsonProperty("projectId") ProjectId projectId) implements Request<UploadLinearizationResponse> {

    public static final String CHANNEL = "webprotege.linearization.ProcessUploadedLinearization";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
