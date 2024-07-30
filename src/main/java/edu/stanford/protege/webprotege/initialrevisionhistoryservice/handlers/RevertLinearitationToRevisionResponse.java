package edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotege.initialrevisionhistoryservice.handlers.RevertLinearitationToRevisionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record RevertLinearitationToRevisionResponse() implements Response {

    public static RevertLinearitationToRevisionResponse create() {
        return new RevertLinearitationToRevisionResponse();
    }
}