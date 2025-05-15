package edu.stanford.protege.webprotege.linearizationservice.handlers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import org.semanticweb.owlapi.model.IRI;
import static edu.stanford.protege.webprotege.linearizationservice.handlers.ContextAwareLinearizationDefinitionRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record ContextAwareLinearizationDefinitionRequest(@JsonProperty("entityIRI") IRI entityIri, ProjectId projectId) implements Request<ContextAwareLinearizationDefinitionResponse>  {
    public static final String CHANNEL = "webprotege.linearization.GetContextAwareLinearizationDefinitions";


    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @Override
    public IRI entityIri() {
        return entityIri;
    }
}
