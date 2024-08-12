package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.Response;

@JsonTypeName(MergeWithParentEntitiesRequest.CHANNEL)
public record MergeWithParentEntitiesResponse() implements Response {
    public static MergeWithParentEntitiesResponse create() {
        return new MergeWithParentEntitiesResponse();
    }

}
