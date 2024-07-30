package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.RevertLinearitationToRevisionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record RevertLinearitationToRevisionRequest(@JsonProperty("entityIri") IRI entityIri,
                                                   @JsonProperty("revisionTimestamp") long revisionTimestamp,
                                                   @JsonProperty("projectId") ProjectId projectId) implements Request<RevertLinearitationToRevisionResponse> {

    public final static String CHANNEL = "webprotege.linearization.RevertLinearitationToRevision";

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
