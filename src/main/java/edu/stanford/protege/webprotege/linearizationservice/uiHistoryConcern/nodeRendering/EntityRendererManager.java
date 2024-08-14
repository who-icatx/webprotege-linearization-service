package edu.stanford.protege.webprotege.linearizationservice.uiHistoryConcern.nodeRendering;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotege.renderer.*;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
public class EntityRendererManager {

    private final Logger logger = LoggerFactory.getLogger(EntityRendererManager.class);

    private final CommandExecutor<GetRenderedOwlEntitiesAction, GetRenderedOwlEntitiesResult> getRenderedEntitiesExecutor;
    private final CommandExecutor<GetEntityHtmlRenderingAction, GetEntityHtmlRenderingResult> getRenderedEntityExecutor;

    public EntityRendererManager(CommandExecutor<GetRenderedOwlEntitiesAction, GetRenderedOwlEntitiesResult> getRenderedEntitiesExecutor,
                                 CommandExecutor<GetEntityHtmlRenderingAction, GetEntityHtmlRenderingResult> getRenderedEntityExecutor) {
        this.getRenderedEntitiesExecutor = getRenderedEntitiesExecutor;
        this.getRenderedEntityExecutor = getRenderedEntityExecutor;
    }


    public CompletableFuture<GetRenderedOwlEntitiesResult> getRenderedEntities(Set<String> entityIris, ProjectId projectId, ExecutionContext executionContext) {
        return getRenderedEntitiesExecutor.execute(
                GetRenderedOwlEntitiesAction.create(entityIris, projectId),
                executionContext
        );
    }

    public CompletableFuture<GetEntityHtmlRenderingResult> getRenderedEntity(IRI entityIri, ProjectId projectId, ExecutionContext executionContext) {
        return getRenderedEntityExecutor.execute(
                new GetEntityHtmlRenderingAction(projectId, new OWLClassImpl(entityIri)),
                executionContext
        );
    }

}